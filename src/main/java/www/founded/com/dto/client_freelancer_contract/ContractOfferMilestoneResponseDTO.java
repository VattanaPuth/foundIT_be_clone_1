package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ContractOfferMilestoneResponseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Integer orderIndex;
}