package www.founded.com.dto.payment;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaywayPayoutRequest {
    private Long withdrawalId;
    private BigDecimal amount;
    private String currency;
    private Long freelancerId;
    private String beneficiariesToken;
}
