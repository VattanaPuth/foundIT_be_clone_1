package www.founded.com.dto.payment.aba;

import lombok.Data;

@Data
public class PaywayPushbackRequest {
    private String tran_id;
    private String apv;
    private String status;      
    private String hash; 
}
