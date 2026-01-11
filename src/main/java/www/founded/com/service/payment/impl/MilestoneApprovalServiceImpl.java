package www.founded.com.service.payment.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.payment.MilestoneStatus;
import www.founded.com.model.payment.Milestone;
import www.founded.com.repository.payment.MilestoneRepository;
import www.founded.com.service.payment.MilestoneApprovalService;

@Service
@RequiredArgsConstructor
public class MilestoneApprovalServiceImpl implements MilestoneApprovalService {

    private final MilestoneRepository milestoneRepo;

    @Override
    @Transactional
    public void submitMilestone(Long milestoneId, Long freelancerId) {

        Milestone ms = milestoneRepo.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        // ✅ Security check: only the milestone freelancer can submit
        if (!ms.getEscrow().getFreelancer().getId().equals(freelancerId)) {
            throw new IllegalStateException("You are not allowed to submit this milestone.");
        }

        if (ms.getStatus() != MilestoneStatus.IN_PROGRESS) {
            throw new IllegalStateException("Milestone must be IN_PROGRESS to submit.");
        }

        ms.setStatus(MilestoneStatus.SUBMITTED);
        ms.setSubmittedAt(Instant.now());
        milestoneRepo.save(ms);
    }

    @Override
    @Transactional
    public void approveMilestone(Long milestoneId, Long clientId) {

        Milestone ms = milestoneRepo.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        // ✅ Security check: only the escrow client can approve
        if (!ms.getEscrow().getClient().getId().equals(clientId)) {
            throw new IllegalStateException("You are not allowed to approve this milestone.");
        }

        if (ms.getStatus() != MilestoneStatus.SUBMITTED) {
            throw new IllegalStateException("Milestone must be SUBMITTED before approval.");
        }

        ms.setStatus(MilestoneStatus.APPROVED);
        ms.setApprovedAt(Instant.now());
        // ms.setApprovedByClientId(clientId);
        milestoneRepo.save(ms);
    }
}
