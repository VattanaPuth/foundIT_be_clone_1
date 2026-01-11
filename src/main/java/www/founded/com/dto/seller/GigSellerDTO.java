package www.founded.com.dto.seller;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class GigSellerDTO {
    private String name;        // Gig's name
    private String description; // Description of the gig/project
    private String serviceType; // Type of service offered
    private BigDecimal price;
    private byte[] imageData;   // Image of the gig
    private boolean isPublic;   // Whether the gig is visible
    private List<ProductDTO> products; // List of products (milestones or tasks)
}