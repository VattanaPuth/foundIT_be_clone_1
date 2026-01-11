package www.founded.com.service.payment.impl;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.payment.MilestoneStatus;
import www.founded.com.enum_.payment.TransactionStatus;
import www.founded.com.enum_.payment.TransactionType;
import www.founded.com.enum_.security.Role;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Milestone;
import www.founded.com.model.payment.Transaction;
import www.founded.com.model.payment.Wallet;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.payment.MilestoneRepository;
import www.founded.com.repository.payment.TransactionRepository;
import www.founded.com.repository.payment.WalletRepository;
import www.founded.com.service.payment.MilestonePaymentService;

@Service
@RequiredArgsConstructor
public class MilestonePaymentServiceImpl implements MilestonePaymentService {

    private final EscrowRepository escrowRepo;
    private final MilestoneRepository milestoneRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository txnRepo;

    @Override
    @Transactional
    public void releaseMilestone(Long milestoneId) {

        Milestone ms = milestoneRepo.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        Escrow escrow = ms.getEscrow();

        // 1) Escrow must be funded / active
        if (escrow.getStatus() != EscrowStatus.FUNDED
                && escrow.getStatus() != EscrowStatus.IN_PROGRESS
                && escrow.getStatus() != EscrowStatus.PARTIALLY_RELEASED) {
            throw new IllegalStateException("Cannot release in escrow status: " + escrow.getStatus());
        }

        // 2) Must be approved by client first
        if (ms.getStatus() != MilestoneStatus.APPROVED) {
            throw new IllegalStateException("Milestone must be APPROVED by client before release.");
        }

        // 3) Prevent double-release (IMPORTANT)
        String releaseKey = "RELEASE:" + ms.getId();
        if (txnRepo.existsByIdempotencyKey(releaseKey)) {
            // already released before
            return;
        }

        // 4) Find/create freelancer wallet
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

        // 5) Credit wallet
        wallet.setBalance(wallet.getBalance().add(ms.getAmount()));
        walletRepo.save(wallet);

        // 6) Ledger transaction
        Transaction t = new Transaction();
        t.setEscrow(escrow);
        t.setWallet(wallet);
        t.setType(TransactionType.ESCROW_RELEASE);
        t.setStatus(TransactionStatus.SUCCESS);
        t.setAmount(ms.getAmount());
        t.setCurrency("USD");
        t.setIdempotencyKey(releaseKey);
        txnRepo.save(t);

        // 7) Update escrow summary
        BigDecimal paid = escrow.getAmountPaidToFreelancer() == null
                ? BigDecimal.ZERO
                : escrow.getAmountPaidToFreelancer();

        escrow.setAmountPaidToFreelancer(paid.add(ms.getAmount()));

        if (escrow.getAmountPaidToFreelancer().compareTo(escrow.getTotalAmount()) >= 0) {
            escrow.setStatus(EscrowStatus.COMPLETED);
        } else {
            escrow.setStatus(EscrowStatus.PARTIALLY_RELEASED);
        }
        escrowRepo.save(escrow);

        // 8) Mark milestone released (✅ missing in your current code)
        ms.setStatus(MilestoneStatus.RELEASED);
        ms.setReleasedAt(Instant.now());   // requires field in Milestone entity
        milestoneRepo.save(ms);
    }
}
