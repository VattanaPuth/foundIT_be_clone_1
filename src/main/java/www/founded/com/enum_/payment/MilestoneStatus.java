package www.founded.com.enum_.payment;

public enum MilestoneStatus {
    NOT_STARTED,
    IN_PROGRESS,
    // freelancer finished milestone and submitted for review
    SUBMITTED,
    // client approved milestone
    APPROVED,
    // system released money to wallet
    RELEASED,
    COMPLETED
}
