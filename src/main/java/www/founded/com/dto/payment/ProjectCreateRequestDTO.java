package www.founded.com.dto.payment;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectCreateRequestDTO {
    @NotNull private Long clientId;
    @NotNull private Long freelancerId;

    @NotBlank private String title;
    private String description;

    @NotNull @DecimalMin("0.01")
    private BigDecimal totalBudget;

    @NotEmpty
    private List<MilestoneCreateDTO> milestones;
}
