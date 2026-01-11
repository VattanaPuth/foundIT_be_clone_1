package www.founded.com.dto.payment;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MilestoneCreateDTO {
    @NotBlank 
    private String description;

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;
}
