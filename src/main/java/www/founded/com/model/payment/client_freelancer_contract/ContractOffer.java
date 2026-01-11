package www.founded.com.model.payment.client_freelancer_contract;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus;
import www.founded.com.enum_.client_freelancer_contract.OfferDirection;
import www.founded.com.enum_.client_freelancer_contract.OfferSourceType;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;

@Entity
@Table(name = "contract_offers",
        indexes = {
                @Index(name="idx_offer_client", columnList="fk_client_id"),
                @Index(name="idx_offer_freelancer", columnList="fk_freelancer_id"),
                @Index(name="idx_offer_source", columnList="source_type,sourceId"),
                @Index(name="idx_offer_status", columnList="status")
        })
@Data
public class ContractOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_client_id", nullable=false)
    private Client clientId;

    @ManyToOne
    @JoinColumn(name="fk_freelancer_id", nullable=false)
    private Freelancer freelancerId;

    @Enumerated(EnumType.STRING)
    @Column(name="direction", nullable=false)
    private OfferDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name="source_type", nullable=false)
    private OfferSourceType sourceType;

    @Column(name="source_id")
    private Long sourceId; // gigId or jobPostId (depending on sourceType)

    @Column(nullable=false)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal totalBudget;

    @Column(length=3, nullable=false)
    private String currency = "USD";

    // extra proposal-like fields
    private Integer deliveryDays;

    @Column(columnDefinition="TEXT")
    private String message; // cover letter / note

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ContractOfferStatus status = ContractOfferStatus.SENT;

    // when accepted -> created projectId
    private Long projectId;

    private Instant expiresAt;
    private Instant acceptedAt;
    private Instant rejectedAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(nullable = false)
    private boolean isPublic; 
}
