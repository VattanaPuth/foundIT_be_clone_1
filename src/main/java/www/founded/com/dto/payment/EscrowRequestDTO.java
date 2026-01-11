package www.founded.com.dto.payment;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EscrowRequestDTO {
	
	@NotNull 
    private Long clientId;
	@NotNull 
	private Long freelancerId;
    @NotNull 
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    private List<MilestoneCreateDTO> milestones;
}
