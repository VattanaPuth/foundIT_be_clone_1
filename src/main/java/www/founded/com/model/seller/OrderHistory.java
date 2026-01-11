package www.founded.com.model.seller;

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


@Entity
@Table(name = "order_history")
@Data
public class OrderHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;  // Reference to the related order
    
    private String action;  // Action description (e.g., "Order created", "Payment confirmed")
    
    private String details;  // Additional details about the action (e.g., reason for cancellation)
    
    private Instant timestamp;  // The timestamp of when the action was performed
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;  // The order status after the action (e.g., "PENDING", "COMPLETED")
}


