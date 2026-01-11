package www.founded.com.model.client;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Table(name = "gig_client")
@Data
public class GigClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String category;
    private String type;
    private String payMode; // "fixed" or "hourly"
    
    private Double budgetMin;
    private Double budgetMax;
    
    private String deliveryTime;
    private String imageType;
    
    @Column(columnDefinition = "bytea")
    private byte[] imageData;
    
    // Reference files stored as JSON string
    @Column(columnDefinition = "TEXT")
    private String referenceFiles; // JSON array of {name, type, data}

    // Mark if the gig is public (visible to freelancers)
    @Column(nullable = false)
    private boolean isPublic = true; // Default to public

    // User who posted the gig (email or username)
    @Column(nullable = false)
    private String postedBy;

    // Creation date
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();
}
