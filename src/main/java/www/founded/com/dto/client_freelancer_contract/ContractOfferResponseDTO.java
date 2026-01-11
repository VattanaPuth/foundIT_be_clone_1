package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Data;
import www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus;

@Data
public class ContractOfferResponseDTO {
    private Long id;
    private Long clientId;
    private Long freelancerId;
    private Long gigId;
    private String title;
    private String description;
    private BigDecimal totalBudget;
    private String currency;
    private ContractOfferStatus status;
    private Long projectId;
    private Instant expiresAt;
    private Instant createdAt;
    private List<ContractOfferMilestoneResponseDTO> milestones;
}
