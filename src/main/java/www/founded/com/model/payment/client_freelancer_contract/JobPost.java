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
import www.founded.com.enum_.client_freelancer_contract.JobPostStatus;
import www.founded.com.model.client.Client;

@Entity
@Table(name="job_posts", indexes = {
        @Index(name="idx_job_client", columnList="client_id"),
        @Index(name="idx_job_status", columnList="status")
})
@Data
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="client_id", nullable=false)
    private Client clientId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal budget;

    @Column(length=3, nullable=false)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private JobPostStatus status = JobPostStatus.OPEN;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
