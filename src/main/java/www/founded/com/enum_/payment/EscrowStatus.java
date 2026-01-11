package www.founded.com.enum_.payment;

public enum EscrowStatus {
	 // Escrow created in DB, but client has NOT paid yet
    CREATED,

    // Client redirected to PayWay checkout
    PENDING_PAYMENT,

    // PayWay confirmed payment (webhook / return URL)
    FUNDED,

    // Work has started (milestones in progress)
    IN_PROGRESS,

    // Some money already released to freelancer
    PARTIALLY_RELEASED,

    // All money released to freelancer
    COMPLETED,

    // Client cancelled before payment
    EXPIRED,

    // Client cancelled after payment (refund logic applied)
    CANCELLED,

    // PayWay error, signature mismatch, or API failure
    FAILED
}
