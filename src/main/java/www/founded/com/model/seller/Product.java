package www.founded.com.model.seller;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private BigDecimal price;
    @Column(nullable = false)
    private boolean isAvailable;  // Availability of the product

    @ManyToOne
    @JoinColumn(name = "gig_seller_id")
    private GigSeller gigSeller;  // GigSeller the product is associated with
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;  // Seller who owns the product
}

