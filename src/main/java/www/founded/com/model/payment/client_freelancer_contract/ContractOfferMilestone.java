package www.founded.com.model.payment.client_freelancer_contract;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="contract_offer_milestones",
        indexes = @Index(name="idx_offer_ms_offer", columnList="offer_id"))
@Data
public class ContractOfferMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_offer_milestone_seq")
    @SequenceGenerator(name = "contract_offer_milestone_seq", sequenceName = "contract_offer_milestone_seq", allocationSize = 1)
    private Long id;

    @Column(name="offer_id", nullable=false)
    private Long offerId;

    @Column(nullable=false)
    private String description;

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal amount;

    @Column(name="order_index", nullable=false)
    private Integer orderIndex;
}
