package www.founded.com.service.payment;

import java.math.BigDecimal;

import www.founded.com.dto.payment.EscrowRequestDTO;
import www.founded.com.dto.payment.PaywayCheckoutResponse;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.seller.Order;

public interface EscrowService {
	Escrow createEscrow(EscrowRequestDTO request);
	void createEscrowForOrder(Order order);
	PaywayCheckoutResponse fundEscrow(Long projectId, String returnUrl, String webhookUrl);
	void releasePayment(Long escrowId, BigDecimal amount);
    void refundPayment(Long escrowId, int step); 
    void cancelEscrowAndSplit(Long escrowId, int step);
}
