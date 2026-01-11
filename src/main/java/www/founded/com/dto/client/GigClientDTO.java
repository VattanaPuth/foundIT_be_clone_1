package www.founded.com.dto.client;

import lombok.Data;

@Data
public class GigClientDTO {
    private String title;
    private String description;
    private String category;
    private String type;
    private String payMode; // "fixed" or "hourly"
    private Double budgetMin;
    private Double budgetMax;
    private String deliveryTime;
    private String imageType;
    private byte[] imageData;
    private String referenceFiles; // JSON string of reference files
}
