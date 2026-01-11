package www.founded.com.model.payment.freelancer_client_contract;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "proposal_milestones")
@Data
public class ProposalMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proposalId;

    private String description;

    private BigDecimal amount;

    private Integer orderIndex;
}

