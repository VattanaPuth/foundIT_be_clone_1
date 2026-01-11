package www.founded.com.model.payment;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.seller.Order;

@Entity
@Table(name = "escrow")
@Data
public class Escrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal totalAmount;
    private BigDecimal amountPaidToFreelancer;
    private BigDecimal amountRefundedToClient;
    @Column(name = "payway_txn_id", unique = true)
    private String paywayTxnId;

    private String paywayCheckoutUrl;

    @Enumerated(EnumType.STRING)
    private EscrowStatus status;
    
    @ManyToOne
    private Client client;
    
    @ManyToOne
    private Freelancer freelancer;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @OneToMany(mappedBy = "escrow", cascade = CascadeType.ALL)
    private List<Milestone> milestones;

}
