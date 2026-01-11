package www.founded.com.dto.payment;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaywayRefundRequest {
	private Long escrowId;
    private BigDecimal amount;
    private String reason;
}
