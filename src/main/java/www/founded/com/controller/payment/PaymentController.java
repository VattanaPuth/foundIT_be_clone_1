package www.founded.com.controller.payment;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.payment.PaywayCheckoutResponse;
import www.founded.com.dto.payment.ProjectCreateRequestDTO;
import www.founded.com.dto.payment.aba.PaywayCheckTxnResponse;
import www.founded.com.dto.payment.aba.PaywayPushbackRequest;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.payment.TransactionStatus;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.Transaction;
import www.founded.com.model.payment.Withdrawal;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.payment.TransactionRepository;
import www.founded.com.service.payment.EscrowService;
import www.founded.com.service.payment.MilestoneApprovalService;
import www.founded.com.service.payment.MilestonePaymentService;
import www.founded.com.service.payment.ProjectService;
import www.founded.com.service.payment.WithdrawalService;
import www.founded.com.service.payment.impl.PaywayClientImpl;
import www.founded.com.utils.payment.aba.PaywaySignatureVerifier;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ProjectService projectService;
    private final EscrowService escrowService;
    private final MilestonePaymentService milestonePaymentService;
    private final MilestoneApprovalService milestoneApprovalService;
    private final WithdrawalService withdrawalService;
    private final PaywaySignatureVerifier paywaySignatureVerifier;

    private final EscrowRepository escrowRepo;
    private final TransactionRepository txnRepo;
    private final PaywayClientImpl payway;

    // 1) Create Project + Escrow + Milestones
    @PostMapping("/projects")
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectCreateRequestDTO req) {
        return ResponseEntity.ok(projectService.createProject(req));
    }

    // 2) Fund Escrow (PayWay checkout)
    @PostMapping("/projects/{projectId}/fund")
    public ResponseEntity<PaywayCheckoutResponse> fundEscrow(
            @PathVariable Long projectId,
            @RequestParam String returnUrl,
            @RequestParam String webhookUrl
    ) {
        return ResponseEntity.ok(escrowService.fundEscrow(projectId, returnUrl, webhookUrl));
    }

    // Freelancer submits milestone
    @PostMapping("/milestones/{milestoneId}/submit")
    public ResponseEntity<Void> submitMilestone(
            @PathVariable Long milestoneId,
            @RequestParam Long freelancerId
    ) {
        milestoneApprovalService.submitMilestone(milestoneId, freelancerId);
        return ResponseEntity.ok().build();
    }

    // Client approves milestone
    @PostMapping("/milestones/{milestoneId}/approve")
    public ResponseEntity<Void> approveMilestone(
            @PathVariable Long milestoneId,
            @RequestParam Long clientId
    ) {
        milestoneApprovalService.approveMilestone(milestoneId, clientId);
        return ResponseEntity.ok().build();
    }

    // 3) Release Milestone (escrow -> freelancer wallet)
    @PostMapping("/milestones/{milestoneId}/release")
    public ResponseEntity<Void> releaseMilestone(@PathVariable Long milestoneId) {
        milestonePaymentService.releaseMilestone(milestoneId);
        return ResponseEntity.ok().build();
    }

    // 4) Refund only (client refund based on step)
    @PostMapping("/escrows/{escrowId}/refund")
    public ResponseEntity<Void> refund(@PathVariable Long escrowId, @RequestParam int step) {
        escrowService.refundPayment(escrowId, step);
        return ResponseEntity.ok().build();
    }

    // 5) Cancel + Split (refund client + credit freelancer wallet)
    @PostMapping("/escrows/{escrowId}/cancel")
    public ResponseEntity<Void> cancelAndSplit(@PathVariable Long escrowId, @RequestParam int step) {
        escrowService.cancelEscrowAndSplit(escrowId, step);
        return ResponseEntity.ok().build();
    }

    // 6) Withdraw (lock funds)
    @PostMapping("/withdrawals/request")
    public ResponseEntity<Withdrawal> requestWithdraw(
            @RequestParam Long walletId,
            @RequestParam BigDecimal amount
    ) {
        return ResponseEntity.ok(withdrawalService.requestWithdraw(walletId, amount));
    }

    // 7) Withdraw payout (call PayWay payout)
    @PostMapping("/withdrawals/{withdrawalId}/process")
    public ResponseEntity<Void> processWithdrawal(@PathVariable Long withdrawalId) {
        withdrawalService.processWithdrawalPayout(withdrawalId);
        return ResponseEntity.ok().build();
    }

    // 8) PayWay webhook (pushback)
    // Recommended path: /payments/payway/webhook
    @PostMapping("/payway/webhook")
    @Transactional
    public ResponseEntity<String> webhook(@RequestBody PaywayPushbackRequest push) {

        // 1) Required fields
        if (push.getTran_id() == null || push.getApv() == null
                || push.getStatus() == null || push.getHash() == null) {
            return ResponseEntity.badRequest().body("Invalid pushback payload");
        }

        // 2) Verify PayWay signature
        boolean validSignature = paywaySignatureVerifier.verifyPushback(
                push.getTran_id(),
                push.getApv(),
                push.getStatus(),
                push.getHash()
        );

        if (!validSignature) {
            return ResponseEntity.status(403).body("Invalid PayWay signature");
        }

        String tranId = push.getTran_id();

        // 3) Verify via PayWay server API
        PaywayCheckTxnResponse verified = payway.checkTransaction(tranId);

        boolean paid = verified.getData() != null
                && "APPROVED".equalsIgnoreCase(verified.getData().getPayment_status());

        if (!paid) {
            return ResponseEntity.ok("IGNORED_NOT_PAID");
        }

        // 4) Find escrow (recommended approach)
        Escrow escrow = escrowRepo.findByPaywayTxnId(tranId)
                .orElseThrow(() -> new IllegalArgumentException("Escrow not found for tran_id: " + tranId));

        // 5) Amount validation (extra safety)
        if (verified.getData().getTotal_amount()
                .compareTo(escrow.getTotalAmount()) != 0) {
            return ResponseEntity.status(409).body("Amount mismatch");
        }

        // 6) Idempotency
        if (escrow.getStatus() == EscrowStatus.FUNDED
                || escrow.getStatus() == EscrowStatus.IN_PROGRESS
                || escrow.getStatus() == EscrowStatus.PARTIALLY_RELEASED
                || escrow.getStatus() == EscrowStatus.COMPLETED) {
            return ResponseEntity.ok("OK_ALREADY_PROCESSED");
        }

        // 7) Mark deposit transaction SUCCESS
        String depositKey = "DEPOSIT:" + escrow.getId();
        Transaction depositTxn = txnRepo.findByIdempotencyKey(depositKey)
                .orElseThrow(() -> new IllegalStateException("Deposit txn not found for " + depositKey));

        depositTxn.setStatus(TransactionStatus.SUCCESS);
        depositTxn.setPaywayTxnId(tranId);
        depositTxn.setRawResponse("apv=" + verified.getData().getApv());
        txnRepo.save(depositTxn);

        // 8) Update escrow
        escrow.setStatus(EscrowStatus.FUNDED);
        escrowRepo.save(escrow);

        return ResponseEntity.ok("OK");
    }
}
