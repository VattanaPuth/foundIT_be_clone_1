package www.founded.com.dto.payment;

import java.math.BigDecimal;

import lombok.Data;

@Data 
public class PaywayCheckoutRequest {
    private Long escrowId;
    private BigDecimal amount;
    private String currency;
    private String returnUrl;
    private String webhookUrl;
}
