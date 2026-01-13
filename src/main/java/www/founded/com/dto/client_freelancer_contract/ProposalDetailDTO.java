package www.founded.com.dto.client_freelancer_contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Data;
import www.founded.com.enum_.client_freelancer_contract.ProposalStatus;

@Data
public class ProposalDetailDTO {
    private Long id;
    private Long jobPostId;
    private String jobTitle;
    private Long clientId;
    private String clientName;
    private Long freelancerId;
    private String freelancerName;
    private String freelancerAvatar;
    private String freelancerBio;
    private String freelancerSkill;
    private Double freelancerRating;
    private Integer freelancerReviewCount;
    private String coverLetter;
    private BigDecimal proposedBudget;
    private String currency;
    private Integer deliveryDays;
    private ProposalStatus status;
    private Long projectId;
    private Instant createdAt;
    private List<ProposalMilestoneDTO> milestones;
}
