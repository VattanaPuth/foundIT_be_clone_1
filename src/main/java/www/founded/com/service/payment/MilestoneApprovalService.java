package www.founded.com.service.payment;

public interface MilestoneApprovalService {
    void submitMilestone(Long milestoneId, Long freelancerId);
    void approveMilestone(Long milestoneId, Long clientId);
}
