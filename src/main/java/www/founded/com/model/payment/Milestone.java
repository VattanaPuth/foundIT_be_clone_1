package www.founded.com.model.payment;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.payment.MilestoneStatus;

@Entity
@Table(name = "milestone")
@Data
public class Milestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    private BigDecimal amount;
    private MilestoneStatus status; 

    @ManyToOne
    private Escrow escrow;
    
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant releasedAt;

}
