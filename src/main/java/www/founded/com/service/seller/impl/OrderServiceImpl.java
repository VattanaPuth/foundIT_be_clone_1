package www.founded.com.service.seller.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.seller.OrderStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Order;
import www.founded.com.model.seller.OrderHistory;
import www.founded.com.repository.client.ClientRepository;
import www.founded.com.repository.freelancer.FreelancerRepository;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.seller.GigSellerRepository;
import www.founded.com.repository.seller.OrderHistoryRepository;
import www.founded.com.repository.seller.OrderRepository;
import www.founded.com.service.seller.OrderService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepo;
	private final EscrowRepository escrowRepo;
	private final OrderHistoryRepository orderHistoryRepo;
	private final ClientRepository clientRepo;
	private final GigSellerRepository gigSellerRepo;
	private final FreelancerRepository freelancerRepo;

	// 1. Place an order and create escrow
	@Override
	public Order placeOrder(Long gigSellerId, Long clientId, BigDecimal amount) {
		GigSeller gigSeller = gigSellerRepo.findById(gigSellerId)
				.orElseThrow(() -> new IllegalArgumentException("Gig Seller not found"));

		Client client = clientRepo.findById(clientId)
				.orElseThrow(() -> new IllegalArgumentException("Client not found"));

		// Create the order
		Order order = new Order();
		order.setGigSeller(gigSeller);
		order.setClient(client);
		order.setAmount(amount);
		order.setStatus(OrderStatus.PENDING);

		// Save the order
		order = orderRepo.save(order);

		// Create escrow for the order
		Escrow escrow = new Escrow();
		escrow.setOrder(order);
		escrow.setTotalAmount(amount);
		escrow.setAmountPaidToFreelancer(BigDecimal.ZERO);
		escrow.setAmountRefundedToClient(BigDecimal.ZERO);
		escrow.setStatus(EscrowStatus.CREATED);
		escrow.setClient(client);
		escrow.setFreelancer(gigSeller.getFreelancer()); // Assuming GigSeller has a freelancer
		escrowRepo.save(escrow);

		// Link the escrow to the order
		order.setEscrow(escrow);
		orderRepo.save(order);

		return order;
	}

	// 2. Cancel order and refund if needed
	@Override
	public void cancelOrder(Long orderId, Long clientId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));

		// Check if the client is the one making the cancellation
		if (!order.getClient().getId().equals(clientId)) {
			throw new IllegalArgumentException("Only the client can cancel the order.");
		}

		// Change order status to CANCELLED
		order.setStatus(OrderStatus.CANCELLED);
		orderRepo.save(order);

		// Process the refund if the escrow is paid (client can cancel before or after
		// payment)
		Escrow escrow = escrowRepo.findByOrder(order)
				.orElseThrow(() -> new IllegalArgumentException("Escrow not found"));

		// If escrow status is FUNDED or IN_PROGRESS, refund the client
		if (escrow.getStatus() == EscrowStatus.FUNDED || escrow.getStatus() == EscrowStatus.IN_PROGRESS) {
			escrow.setAmountRefundedToClient(escrow.getTotalAmount());
			escrow.setStatus(EscrowStatus.CANCELLED); // Mark the escrow as cancelled
			escrowRepo.save(escrow);
		}
	}

	// 3. Release payment to the seller (after order completion)
	@Override
	public Escrow releasePayment(Long orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));

		Escrow escrow = escrowRepo.findByOrder(order)
				.orElseThrow(() -> new IllegalArgumentException("Escrow not found"));

		if (escrow.getStatus() == EscrowStatus.FUNDED) {
			// Transfer the amount to freelancer and mark escrow as completed
			escrow.setAmountPaidToFreelancer(escrow.getTotalAmount());
			escrow.setStatus(EscrowStatus.COMPLETED);
			escrowRepo.save(escrow);
		}

		return escrow;
	}

	// Optional: you can add methods for getting all orders by a client or seller
	@Override
	public List<Order> getOrdersByClient(Long clientId) {
		Client client = clientRepo.findById(clientId)
				.orElseThrow(() -> new IllegalArgumentException("Client not found"));

		return orderRepo.findByClient(client);
	}

	@Override
	public Order updateOrderStatus(Long orderId, OrderStatus status) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
		order.setStatus(status);
		order.setUpdatedAt(Instant.now());
		orderRepo.save(order);

		// Log the action to history
		createOrderHistory(order, "Order status updated", "Order status changed to " + status);

		return order;
	}

	@Override
	public List<Order> getOrdersBySeller(Long sellerId) {
		return orderRepo.findByGigSellerId(sellerId); // Custom query to get orders by gig seller
	}

	@Override
	public List<OrderHistory> getOrderHistory(Long orderId) {
		return orderHistoryRepo.findByOrderId(orderId);
	}

	private void createOrderHistory(Order order, String action, String description) {
		OrderHistory history = new OrderHistory();
		history.setOrder(order);
		history.setAction(action);
		history.setTimestamp(Instant.now()); // Set the current timestamp
		orderHistoryRepo.save(history); // Save the order history entry
	}

	@Override
	public List<Order> getOrdersByFreelancer(Long freelancerId) {
		Freelancer freelancer = freelancerRepo.findById(freelancerId)
                .orElseThrow(() -> new IllegalArgumentException("Freelancer not found"));
        return orderRepo.findByFreelancer(freelancer);  // Fetch orders assigned to the freelancer
    }
}
