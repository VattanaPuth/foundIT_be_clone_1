package www.founded.com.dto.seller;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductDTO {
    private String title;
    private String description;
    private BigDecimal price;
    private boolean isAvailable;
    private Long sellerId;
}
