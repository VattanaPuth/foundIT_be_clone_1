package www.founded.com.dto.payment;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import www.founded.com.enum_.payment.EscrowStatus;

@Data
public class EscrowResponseDTO {
    private Long id;
    private Long clientId;
    private Long freelancerId;
    private BigDecimal totalAmount;
    private BigDecimal amountPaidToFreelancer;
    private BigDecimal amountRefundedToClient;
    private EscrowStatus status;
    
    // if you store Payway refs:
    private String paywayTxnId;
    private String paywayCheckoutUrl;

    private List<MilestoneResponseDTO> milestones;
}
