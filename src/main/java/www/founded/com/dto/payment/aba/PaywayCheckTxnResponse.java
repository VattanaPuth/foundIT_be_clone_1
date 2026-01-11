package www.founded.com.dto.payment.aba;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaywayCheckTxnResponse {
    private DataBlock data;
    private StatusBlock status;

    @Data
    public static class DataBlock {
        private Integer payment_status_code;
        private String payment_status; // "APPROVED"
        private BigDecimal total_amount;
        private BigDecimal refund_amount;
        private String payment_currency;
        private String apv;
        private String transaction_date;
    }

    @Data
    public static class StatusBlock {
        private String code;     // "00"
        private String message;
        private String tran_id;
    }
}
