package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientOfferCreateDTO {

    @NotNull
    private Long clientId;

    @NotNull
    private Long freelancerId;

    // optional: gigId
    private Long gigId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal totalBudget;

    private String currency = "USD";

    private Instant expiresAt;

    @NotEmpty
    private List<ClientOfferMilestoneCreateDTO> milestones;

    // optional note to freelancer
    private String message;
}
