package www.founded.com.dto.payment;

import java.math.BigDecimal;

import lombok.Data;
import www.founded.com.enum_.payment.MilestoneStatus;

@Data
public class MilestoneResponseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private MilestoneStatus status;
}

