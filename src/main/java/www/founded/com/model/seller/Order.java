package www.founded.com.model.seller;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.seller.OrderStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Escrow;

@Entity
@Table(name = "orders")
@Data
public class Order {
    public String getTitle() {
        return projectTitle;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "gig_seller_id")
    private GigSeller gigSeller; // The gig that the client orders

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client; // The client who orders the gig
    
    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private Freelancer freelancer;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status; 
    
    @ManyToOne
    @JoinColumn(name = "escrow_id")
    private Escrow escrow;

    private BigDecimal amount; // The amount for the order
    private BigDecimal price;
    
    private String projectTitle;  // Project title (could be provided by the client)
    private BigDecimal totalAmount;  // Total price of the order

    private Instant createdAt;  // Timestamp when the order was created
    private Instant updatedAt; 
}
