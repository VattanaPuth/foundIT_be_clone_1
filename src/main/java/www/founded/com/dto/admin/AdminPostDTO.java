package www.founded.com.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminPostDTO {
    private String postType; // e.g. "GIG_SELLER", "GIG_FREELANCER", "ORDER"
    private Long postId;
    private String description;    // optional
    private String status;   // optional
    private BigDecimal amount; // optional
    private Instant createdAt; // optional
}
