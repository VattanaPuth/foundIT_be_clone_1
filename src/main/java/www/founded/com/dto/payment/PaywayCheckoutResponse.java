package www.founded.com.dto.payment;

import lombok.Data;

@Data
public class PaywayCheckoutResponse {
    private String txnId;
    private String checkoutUrl;
    private String checkoutHtml;
}
