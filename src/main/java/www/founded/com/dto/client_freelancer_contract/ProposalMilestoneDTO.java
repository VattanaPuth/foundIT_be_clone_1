package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProposalMilestoneDTO {
    private Long id;
    private Long offerId;
    private String description;
    private BigDecimal amount;
    private Integer orderIndex;
}
