package www.founded.com.service.payment.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.payment.EscrowRequestDTO;
import www.founded.com.dto.payment.MilestoneCreateDTO;
import www.founded.com.dto.payment.PaywayCheckoutRequest;
import www.founded.com.dto.payment.PaywayCheckoutResponse;
import www.founded.com.dto.payment.PaywayRefundRequest;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.payment.MilestoneStatus;
import www.founded.com.enum_.payment.TransactionStatus;
import www.founded.com.enum_.payment.TransactionType;
import www.founded.com.enum_.security.Role;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Milestone;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.Transaction;
import www.founded.com.model.payment.Wallet;
import www.founded.com.model.seller.Order;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.payment.MilestoneRepository;
import www.founded.com.repository.payment.ProjectRepository;
import www.founded.com.repository.payment.TransactionRepository;
import www.founded.com.repository.payment.WalletRepository;
import www.founded.com.service.client.ClientService;
import www.founded.com.service.freelancer.FreelancerService;
import www.founded.com.service.payment.EscrowService;
import www.founded.com.service.payment.PaywayClient;


@Service
@RequiredArgsConstructor
public class EscrowServiceImpl implements EscrowService{
	
	private final EscrowRepository escrowRepo;
    private final MilestoneRepository milestoneRepository;
    private final ClientService clientService;
    private final FreelancerService freelancerService;
    private final RestTemplate restTemplate;
    
    private final ProjectRepository projectRepo;
    private final TransactionRepository txnRepo;
    private final PaywayClient payway;
    private final WalletRepository walletRepo;

    @Value("${aba.payway.api.endpoint}")
    private String apiEndpoint;
	
 
	@Override
	public Escrow createEscrow(EscrowRequestDTO request) {
        Client client = clientService.getById(request.getClientId());
        Freelancer freelancer = freelancerService.getById(request.getFreelancerId());

        Escrow escrow = new Escrow();
        escrow.setClient(client);
        escrow.setFreelancer(freelancer);
        escrow.setTotalAmount(request.getAmount());
        escrow.setAmountPaidToFreelancer(BigDecimal.ZERO);
        escrow.setAmountRefundedToClient(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.CREATED);

        escrow = escrowRepo.save(escrow);

        // Save milestones (optional)
        if (request.getMilestones() != null && !request.getMilestones().isEmpty()) {
            for (MilestoneCreateDTO m : request.getMilestones()) {
                Milestone ms = new Milestone();
                ms.setEscrow(escrow);
                ms.setDescription(m.getDescription());
                ms.setAmount(m.getAmount());
                ms.setStatus(MilestoneStatus.NOT_STARTED);
                milestoneRepository.save(ms);
            }
        }

        // REAL integration note:
        // PayWay usually works as "create checkout" -> redirect user -> callback/webhook (IPN) -> confirm.
        // So here you normally generate a checkout URL, not instantly assume "OK".

        // Payload that contained data
        // Object works with the deserialize data, readValue
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", request.getAmount().toPlainString());
        payload.put("escrowId", escrow.getId());
        payload.put("clientId", client.getId());
        payload.put("freelancerId", freelancer.getId());

        // real path url fron abe payway
        String url = apiEndpoint + "/create-checkout"; // <-- use PayWay real path from their docs
        ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // example expected fields (depends on PayWay):
        	
        	//get from aba payway and set into escrow database, keep the documents
            String txnId = Objects.toString(response.getBody().get("txnId"), null); 
            String checkoutUrl = Objects.toString(response.getBody().get("checkoutUrl"), null);

            escrow.setPaywayTxnId(txnId);
            escrow.setPaywayCheckoutUrl(checkoutUrl);
            escrow.setStatus(EscrowStatus.PENDING_PAYMENT); // better than IN_PROGRESS immediately
            return escrowRepo.save(escrow);
        }

        escrow.setStatus(EscrowStatus.FAILED);
        return escrowRepo.save(escrow);
    }
	
	@Transactional
	public PaywayCheckoutResponse fundEscrow(Long projectId, String returnUrl, String webhookUrl) {

	    Project project = projectRepo.findById(projectId)
	            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

	    Escrow escrow = project.getEscrow();
	    if (escrow == null) {
	        throw new IllegalStateException("Project has no escrow");
	    }

	    if (escrow.getStatus() != EscrowStatus.CREATED && escrow.getStatus() != EscrowStatus.EXPIRED) {
	        throw new IllegalStateException("Escrow cannot be funded in status: " + escrow.getStatus());
	    }

	    // Idempotency for deposit transaction
	    String depositKey = "DEPOSIT:" + escrow.getId();
	    Transaction t = txnRepo.findByIdempotencyKey(depositKey).orElse(null);

	    if (t == null) {
	        t = new Transaction();
	        t.setProject(project);
	        t.setEscrow(escrow);
	        t.setType(TransactionType.ESCROW_DEPOSIT);
	        t.setStatus(TransactionStatus.PENDING);
	        t.setAmount(escrow.getTotalAmount());
	        t.setCurrency("USD");
	        t.setIdempotencyKey(depositKey);
	        t = txnRepo.save(t);
	    }

	    // move escrow to pending payment BEFORE calling PayWay
	    escrow.setStatus(EscrowStatus.PENDING_PAYMENT);
	    escrowRepo.save(escrow);

	    PaywayCheckoutRequest req = new PaywayCheckoutRequest();
	    req.setEscrowId(escrow.getId());
	    req.setAmount(escrow.getTotalAmount());
	    req.setCurrency("USD");
	    req.setReturnUrl(returnUrl);
	    req.setWebhookUrl(webhookUrl);

	    PaywayCheckoutResponse res = payway.createCheckout(req);

	    //store PayWay txn id (tran_id) for webhook lookup
	    escrow.setPaywayTxnId(res.getTxnId());

	    // store either checkoutUrl or checkoutHtml depending on your client
	    escrow.setPaywayCheckoutUrl(res.getCheckoutUrl()); // might be null if you return HTML
	    // escrow.setPaywayCheckoutHtml(res.getCheckoutHtml()); // if you added this field

	    escrowRepo.save(escrow);

	    // keep txn pending until webhook confirms
	    t.setPaywayTxnId(res.getTxnId());
	    t.setRawResponse(
	            res.getCheckoutUrl() != null
	                    ? "checkoutUrl=" + res.getCheckoutUrl()
	                    : "checkoutHtml=" + (res.getCheckoutHtml() != null ? "YES" : "NO")
	    );
	    txnRepo.save(t);

	    return res;
	}

	@Override
	public void releasePayment(Long escrowId, BigDecimal amount) {
        Escrow escrow = escrowRepo.findById(escrowId)
                .orElseThrow(() -> new IllegalArgumentException("Escrow not found"));

        if (escrow.getStatus() != EscrowStatus.FUNDED
        	    && escrow.getStatus() != EscrowStatus.IN_PROGRESS
        	    && escrow.getStatus() != EscrowStatus.PARTIALLY_RELEASED) {
        	    throw new IllegalStateException("Refund not allowed for status: " + escrow.getStatus());
        	}


        Map<String, Object> payload = new HashMap<>();
        payload.put("escrowId", escrowId);
        payload.put("amount", amount.toPlainString());

        String url = apiEndpoint + "/release";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Payment release failed");
        }

        escrow.setAmountPaidToFreelancer(escrow.getAmountPaidToFreelancer().add(amount));
        escrowRepo.save(escrow);
    }
	
	@Override
	@Transactional
    public void refundPayment(Long escrowId, int step) {

        Escrow escrow = escrowRepo.findById(escrowId)
                .orElseThrow(() -> new IllegalArgumentException("Escrow not found"));

        if (escrow.getStatus() == EscrowStatus.CANCELLED) {
            throw new IllegalStateException("Already cancelled.");
        }

        if (escrow.getStatus() != EscrowStatus.FUNDED
                && escrow.getStatus() != EscrowStatus.IN_PROGRESS
                && escrow.getStatus() != EscrowStatus.PARTIALLY_RELEASED) {
            throw new IllegalStateException("Refund not allowed in status: " + escrow.getStatus());
        }

        BigDecimal refundPercent = switch (step) {
            case 1 -> new BigDecimal("1.00");
            case 2 -> new BigDecimal("0.50");
            case 3 -> new BigDecimal("0.25");
            case 4 -> new BigDecimal("0.10");
            default -> throw new IllegalArgumentException("Step must be 1..4");
        };

        BigDecimal total = escrow.getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal alreadyRefunded = Optional.ofNullable(escrow.getAmountRefundedToClient()).orElse(BigDecimal.ZERO);
        BigDecimal alreadyPaid = Optional.ofNullable(escrow.getAmountPaidToFreelancer()).orElse(BigDecimal.ZERO);

        BigDecimal remaining = total.subtract(alreadyRefunded).subtract(alreadyPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalStateException("No funds remaining.");

        BigDecimal targetRefund = total.multiply(refundPercent).setScale(2, RoundingMode.HALF_UP);
        BigDecimal refundToDo = targetRefund.subtract(alreadyRefunded);
        if (refundToDo.compareTo(BigDecimal.ZERO) < 0) refundToDo = BigDecimal.ZERO;
        if (refundToDo.compareTo(remaining) > 0) refundToDo = remaining;

        // transaction row (PENDING)
        Transaction tx = new Transaction();
        tx.setEscrow(escrow);
        tx.setType(TransactionType.ESCROW_REFUND);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setAmount(refundToDo);
        tx.setCurrency("USD");
        tx.setIdempotencyKey("REFUND:" + escrowId + ":" + step);
        tx = txnRepo.save(tx);

        // PayWay refund
        payway.refund(new PaywayRefundRequest(escrowId, refundToDo, "Cancel step " + step));

        // mark success + update escrow
        tx.setStatus(TransactionStatus.SUCCESS);
        txnRepo.save(tx);

        escrow.setAmountRefundedToClient(alreadyRefunded.add(refundToDo));
        escrow.setStatus(EscrowStatus.CANCELLED);
        escrowRepo.save(escrow);
    }
	
	@Transactional
    public void cancelEscrowAndSplit(Long escrowId, int step) {

        Escrow escrow = escrowRepo.findById(escrowId)
                .orElseThrow(() -> new IllegalArgumentException("Escrow not found"));

        if (escrow.getStatus() == EscrowStatus.CANCELLED) {
            throw new IllegalStateException("Escrow already cancelled.");
        }

        // Only allow cancel after money exists
        if (escrow.getStatus() != EscrowStatus.FUNDED
                && escrow.getStatus() != EscrowStatus.IN_PROGRESS
                && escrow.getStatus() != EscrowStatus.PARTIALLY_RELEASED) {
            throw new IllegalStateException("Cancel not allowed in status: " + escrow.getStatus());
        }

        // Step → refund percent
        BigDecimal refundPercent = switch (step) {
            case 1 -> new BigDecimal("1.00"); // 100% back to client
            case 2 -> new BigDecimal("0.50"); // 50%
            case 3 -> new BigDecimal("0.25"); // 25%
            case 4 -> new BigDecimal("0.10"); // 10%
            default -> throw new IllegalArgumentException("Step must be 1..4");
        };

        BigDecimal total = escrow.getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal alreadyRefunded = Optional.ofNullable(escrow.getAmountRefundedToClient()).orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal alreadyPaid = Optional.ofNullable(escrow.getAmountPaidToFreelancer()).orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        // Remaining money still available in escrow system
        BigDecimal remaining = total.subtract(alreadyRefunded).subtract(alreadyPaid)
                .setScale(2, RoundingMode.HALF_UP);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No remaining funds to split.");
        }

        // Refund is based on TOTAL (your rule), but cannot exceed remaining
        BigDecimal targetRefund = total.multiply(refundPercent).setScale(2, RoundingMode.HALF_UP);
        BigDecimal refundToDo = targetRefund.subtract(alreadyRefunded).setScale(2, RoundingMode.HALF_UP);
        if (refundToDo.compareTo(BigDecimal.ZERO) < 0) refundToDo = BigDecimal.ZERO;
        if (refundToDo.compareTo(remaining) > 0) refundToDo = remaining;

        // Freelancer gets the rest of remaining (not exceeding remaining)
        BigDecimal payoutToWallet = remaining.subtract(refundToDo).setScale(2, RoundingMode.HALF_UP);

        // -----------------------
        // (A) REFUND CLIENT via PayWay
        // -----------------------
        if (refundToDo.compareTo(BigDecimal.ZERO) > 0) {

            // ledger row PENDING
            Transaction refundTx = new Transaction();
            refundTx.setEscrow(escrow);
            refundTx.setType(TransactionType.ESCROW_REFUND);
            refundTx.setStatus(TransactionStatus.PENDING);
            refundTx.setAmount(refundToDo);
            refundTx.setCurrency("USD");
            refundTx.setIdempotencyKey("CANCEL_REFUND:" + escrowId + ":" + step);
            refundTx = txnRepo.save(refundTx);

            try {
                payway.refund(new PaywayRefundRequest(escrowId, refundToDo, "Cancel step " + step));

                refundTx.setStatus(TransactionStatus.SUCCESS);
                txnRepo.save(refundTx);

                escrow.setAmountRefundedToClient(alreadyRefunded.add(refundToDo));
            } catch (Exception ex) {
                refundTx.setStatus(TransactionStatus.FAILED);
                refundTx.setRawResponse(ex.getMessage());
                txnRepo.save(refundTx);
                throw ex;
            }
        }

        // -----------------------
        // (B) PAY FREELANCER remaining -> INTERNAL WALLET
        // -----------------------
        if (payoutToWallet.compareTo(BigDecimal.ZERO) > 0) {
            Long freelancerId = escrow.getFreelancer().getId();

            Wallet wallet = walletRepo.findByOwnerIdAndOwnerRole(freelancerId, Role.FREELANCER)
                    .orElseGet(() -> {
                        Wallet w = new Wallet();
                        w.setOwnerId(freelancerId);
                        w.setOwnerRole(Role.FREELANCER);
                        w.setBalance(BigDecimal.ZERO);
                        w.setLockedBalance(BigDecimal.ZERO);
                        w.setCurrency("USD");
                        return walletRepo.save(w);
                    });

            // IMPORTANT: prevent double-credit if endpoint called twice
            String releaseKey = "CANCEL_RELEASE:" + escrowId + ":" + step;
            if (!txnRepo.existsByIdempotencyKey(releaseKey)) {

                wallet.setBalance(wallet.getBalance().add(payoutToWallet));
                walletRepo.save(wallet);

                Transaction releaseTx = new Transaction();
                releaseTx.setEscrow(escrow);
                releaseTx.setWallet(wallet);
                releaseTx.setType(TransactionType.ESCROW_RELEASE);
                releaseTx.setStatus(TransactionStatus.SUCCESS);
                releaseTx.setAmount(payoutToWallet);
                releaseTx.setCurrency("USD");
                releaseTx.setIdempotencyKey(releaseKey);
                txnRepo.save(releaseTx);

                escrow.setAmountPaidToFreelancer(alreadyPaid.add(payoutToWallet));
            }
        }

        // Final status
        escrow.setStatus(EscrowStatus.CANCELLED);
        escrowRepo.save(escrow);
    }

	@Override
	public void createEscrowForOrder(Order order) {
		// Create an escrow for the order
        Escrow escrow = new Escrow();
        escrow.setOrder(order);
        escrow.setTotalAmount(order.getPrice());  // Assuming the price is set for the order
        escrow.setAmountPaidToFreelancer(BigDecimal.ZERO);
        escrow.setAmountRefundedToClient(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.CREATED);
        
        // Call PayWay to create a checkout for the payment
        PaywayCheckoutRequest req = new PaywayCheckoutRequest();
        req.setEscrowId(escrow.getId());
        req.setAmount(order.getPrice());
        req.setCurrency("USD");
        payway.createCheckout(req);
        
        // Save the escrow
        escrowRepo.save(escrow);
    }
}
