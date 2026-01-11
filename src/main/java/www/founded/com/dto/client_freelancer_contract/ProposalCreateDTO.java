package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProposalCreateDTO {

    @NotNull
    private Long jobPostId;

    @NotNull
    private Long freelancerId;

    @NotBlank
    private String message; // cover letter

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal proposedBudget;

    private String currency = "USD";

    private Integer deliveryDays;

    @NotEmpty
    private List<ProposalMilestoneCreateDTO> milestones;
}
