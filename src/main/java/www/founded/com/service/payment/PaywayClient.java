package www.founded.com.service.payment;

import www.founded.com.dto.payment.PaywayCheckoutRequest;
import www.founded.com.dto.payment.PaywayCheckoutResponse;
import www.founded.com.dto.payment.PaywayPayoutRequest;
import www.founded.com.dto.payment.PaywayRefundRequest;

public interface PaywayClient {
    PaywayCheckoutResponse createCheckout(PaywayCheckoutRequest req);
    void refund(PaywayRefundRequest req);
    void payout(PaywayPayoutRequest req);   // withdrawal payout
}
