package www.founded.com.model.seller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.model.freelancer.Freelancer;

@Entity
@Table(name = "seller_gig")
@Data
public class GigSeller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_gig_id")
    private Long id;
    
    private String name;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;  // Seller associated with this gig

    private String description;
    private String serviceType;  // Type of service like "Logo Design"
    private byte[] imageData;  // Image representing the gig
    private BigDecimal price;  // Price for the gig
    
    @Column(nullable = false)
    private boolean isPublic;  // Whether the seller’s gig is public

    @OneToMany(mappedBy = "gigSeller")
    private List<Product> products;  // List of products (gigs) offered by the seller
    
    @OneToMany(mappedBy = "gigSeller")
    private List<Order> orders; // List of orders placed by clients
    
    @ManyToOne
    @JoinColumn(name = "freelancer_id") // Assumes there's a freelancer_id column
    private Freelancer freelancer; 
}
