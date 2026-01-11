package www.founded.com.model.payment.freelancer_client_contract;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.client_freelancer_contract.ProposalStatus;

@Entity
@Table(name = "proposals",
       indexes = {
           @Index(name="idx_prop_job", columnList="job_post_id"),
           @Index(name="idx_prop_freelancer", columnList="freelancer_id")
       })
@Data
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobPostId;

    private Long clientId;

    private Long freelancerId;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private BigDecimal proposedBudget;

    private String currency = "USD";

    private Integer deliveryDays;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    // When accepted → linked to project
    private Long projectId;

    @CreationTimestamp
    private Instant createdAt;
}

