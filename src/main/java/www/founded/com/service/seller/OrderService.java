package www.founded.com.service.seller;

import java.math.BigDecimal;
import java.util.List;

import www.founded.com.enum_.seller.OrderStatus;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.seller.Order;
import www.founded.com.model.seller.OrderHistory;

public interface OrderService {
    Order placeOrder(Long gigSellerId, Long clientId, BigDecimal amount);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    void cancelOrder(Long orderId, Long clientId);
    Escrow releasePayment(Long orderId);
    List<Order> getOrdersByClient(Long clientId);
    List<Order> getOrdersByFreelancer(Long freelancerId);
    List<Order> getOrdersBySeller(Long sellerId);
    List<OrderHistory> getOrderHistory(Long orderId);
}
