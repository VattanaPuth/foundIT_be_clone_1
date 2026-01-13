package www.founded.com.controller.seller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.seller.OrderStatus;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.seller.Order;
import www.founded.com.model.seller.OrderHistory;
import www.founded.com.service.seller.OrderService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// Client places an order
	@PostMapping("/create")
	public ResponseEntity<Order> createOrder(@RequestParam Long gigSellerId, @RequestParam Long clientId,
			@RequestParam BigDecimal amount) {
		Order order = orderService.placeOrder(gigSellerId, clientId, amount);
		return ResponseEntity.ok(order);
	}

	@GetMapping("/seller/{sellerId}")
	public ResponseEntity<List<Order>> getOrdersBySeller(@PathVariable Long sellerId) {
		return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId));
	}

	// Get all orders for a client
	@GetMapping("/client/{clientId}")
	public ResponseEntity<List<Order>> getOrdersByClient(@PathVariable Long clientId) {
		return ResponseEntity.ok(orderService.getOrdersByClient(clientId));
	}

	// Get orders for a freelancer
	@GetMapping("/freelancer/{freelancerId}")
	public ResponseEntity<List<Order>> getOrdersByFreelancer(@PathVariable Long freelancerId) {
		List<Order> orders = orderService.getOrdersByFreelancer(freelancerId);
		return ResponseEntity.ok(orders);
	}

	// Update order status
	@PutMapping("/{orderId}/status")
	public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
		return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
	}

	// Client cancels an order
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId, @RequestParam Long clientId) {
		orderService.cancelOrder(orderId, clientId);
		return ResponseEntity.ok().build();
	}

	// Get order history
	@GetMapping("/{orderId}/history")
	public ResponseEntity<List<OrderHistory>> getOrderHistory(@PathVariable Long orderId) {
		return ResponseEntity.ok(orderService.getOrderHistory(orderId));
	}

	// Release payment to the seller
	@PostMapping("/{orderId}/release-payment")
	public ResponseEntity<Escrow> releasePayment(@PathVariable Long orderId) {
		Escrow escrow = orderService.releasePayment(orderId);
		return ResponseEntity.ok(escrow);
	}
	
	// Search orders by query (for global search)
	@GetMapping("/search")
	public ResponseEntity<List<Order>> searchOrders(@RequestParam("query") String query) {
		// Example: search by title or id (customize as needed)
		List<Order> results = orderService.searchOrders(query);
		return ResponseEntity.ok(results);
	}
}
