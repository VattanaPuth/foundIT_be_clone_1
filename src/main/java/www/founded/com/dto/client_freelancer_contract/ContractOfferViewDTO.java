package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Data;
import www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus;
import www.founded.com.enum_.client_freelancer_contract.OfferDirection;

@Data
public class ContractOfferViewDTO {
    private Long id;
    
    // Client info
    private Long clientId;
    private String clientName;
    
    // Freelancer info
    private Long freelancerId;
    private String freelancerName;
    
    // Gig info (if from gig)
    private Long gigId;
    private String gigTitle;
    
    private OfferDirection direction;
    private String title;
    private String description;
    private BigDecimal totalBudget;
    private String currency;
    private String message;
    
    private ContractOfferStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    
    private List<ContractOfferMilestoneViewDTO> milestones;
}
