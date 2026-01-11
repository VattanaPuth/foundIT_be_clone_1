package www.founded.com.service.payment.impl;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.payment.PaywayPayoutRequest;
import www.founded.com.enum_.payment.TransactionStatus;
import www.founded.com.enum_.payment.TransactionType;
import www.founded.com.enum_.payment.WithdrawalStatus;
import www.founded.com.model.payment.Transaction;
import www.founded.com.model.payment.Wallet;
import www.founded.com.model.payment.Withdrawal;
import www.founded.com.model.payment.aba.FreelancerPayout;
import www.founded.com.repository.payment.FreelancerPayoutRepository;
import www.founded.com.repository.payment.TransactionRepository;
import www.founded.com.repository.payment.WalletRepository;
import www.founded.com.repository.payment.WithdrawalRepository;
import www.founded.com.service.payment.PaywayClient;
import www.founded.com.service.payment.WithdrawalService;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WalletRepository walletRepo;
    private final WithdrawalRepository withdrawalRepo;
    private final TransactionRepository txnRepo;
    private final PaywayClient payway;
    private final FreelancerPayoutRepository payoutRepo;

    @Override
    @Transactional
    public Withdrawal requestWithdraw(Long walletId, BigDecimal amount) {

        // Prevent parallel processing withdrawals
        if (withdrawalRepo.existsByWallet_IdAndStatus(walletId, WithdrawalStatus.PROCESSING)) {
            throw new IllegalStateException("Another withdrawal is already processing.");
        }

        Wallet wallet = walletRepo.findByIdForUpdate(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        // Lock funds
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setLockedBalance(wallet.getLockedBalance().add(amount));
        walletRepo.save(wallet);

        // Create withdrawal
        Withdrawal w = new Withdrawal();
        w.setWallet(wallet);
        w.setAmount(amount);
        w.setCurrency(wallet.getCurrency());
        w.setStatus(WithdrawalStatus.REQUESTED);
        w = withdrawalRepo.save(w);

        // Ledger record
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setType(TransactionType.WITHDRAW_REQUEST);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setAmount(amount);
        tx.setCurrency(wallet.getCurrency());
        tx.setIdempotencyKey("WITHDRAW_REQ:" + w.getId());
        txnRepo.save(tx);

        return w;
    }

    @Override
    @Transactional
    public void processWithdrawalPayout(Long withdrawalId) {

        Withdrawal w = withdrawalRepo.findById(withdrawalId)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal not found"));

        if (w.getStatus() != WithdrawalStatus.REQUESTED) {
            throw new IllegalStateException("Withdrawal must be REQUESTED before payout");
        }

        Wallet wallet = walletRepo.findByIdForUpdate(w.getWallet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Mark processing
        w.setStatus(WithdrawalStatus.PROCESSING);
        withdrawalRepo.save(w);

        // Ledger payout record (PENDING)
        Transaction payoutTx = new Transaction();
        payoutTx.setWallet(wallet);
        payoutTx.setType(TransactionType.WITHDRAW_PAYOUT);
        payoutTx.setStatus(TransactionStatus.PENDING);
        payoutTx.setAmount(w.getAmount());
        payoutTx.setCurrency(w.getCurrency());
        payoutTx.setIdempotencyKey("WITHDRAW_PAYOUT:" + w.getId());
        payoutTx = txnRepo.save(payoutTx);

        try {
        	
        	FreelancerPayout payout = payoutRepo.findByFreelancer_Id(wallet.getOwnerId())
                     .orElseThrow(() -> new IllegalStateException("Freelancer payout not configured"));

        	
            // Call PayWay payout
            PaywayPayoutRequest req = new PaywayPayoutRequest();
            req.setWithdrawalId(w.getId());
            req.setAmount(w.getAmount());
            req.setCurrency(w.getCurrency());
            req.setFreelancerId(wallet.getOwnerId());
            req.setBeneficiariesToken(payout.getBeneficiariesToken());

            payway.payout(req);

            // Success: finalize wallet locked funds
            wallet.setLockedBalance(wallet.getLockedBalance().subtract(w.getAmount()));
            walletRepo.save(wallet);

            w.setStatus(WithdrawalStatus.PAID);
            w.setProcessedAt(Instant.now());
            withdrawalRepo.save(w);

            payoutTx.setStatus(TransactionStatus.SUCCESS);
            txnRepo.save(payoutTx);

        } catch (Exception ex) {
            // Failure: unlock funds back to balance
            wallet.setLockedBalance(wallet.getLockedBalance().subtract(w.getAmount()));
            wallet.setBalance(wallet.getBalance().add(w.getAmount()));
            walletRepo.save(wallet);

            w.setStatus(WithdrawalStatus.FAILED);
            withdrawalRepo.save(w);

            payoutTx.setStatus(TransactionStatus.FAILED);
            payoutTx.setRawResponse(ex.getMessage());
            txnRepo.save(payoutTx);

            throw ex;
        }
    }
}
