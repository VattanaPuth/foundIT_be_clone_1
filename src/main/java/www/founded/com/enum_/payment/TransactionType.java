package www.founded.com.enum_.payment;

public enum TransactionType {
    ESCROW_DEPOSIT,      // client funds escrow
    ESCROW_RELEASE,      // escrow -> freelancer wallet
    ESCROW_REFUND,       // escrow -> client
    WITHDRAW_REQUEST,    // freelancer requests withdraw
    WITHDRAW_PAYOUT,     // payout executed
    WITHDRAW_FAILED,
    FEE_CHARGE           // platform fee
}
