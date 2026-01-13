package www.founded.com.dto.freelancer;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GigFreelancerClientViewDTO {
    private Long id;
    private Long freelancerId; // Added for contract/offer functionality
    private String freelancerName;
    private String shortBio;
    private String description;
    private BigDecimal price;
    private String skillName;
    private String imageType;
    private byte[] imageData;
    private String imageUrl; // URL for profile image (can be external or generated)
    private boolean verified;
    
    // Additional fields for client view (can be populated from freelancer profile)
    private Double rating;
    private Integer reviewCount;
    private String experience;
    private String location;
    private Integer lastActiveDays;
    private Integer workCount;
}
