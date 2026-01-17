package www.founded.com.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import www.founded.com.model.seller.Order;
import www.founded.com.service.seller.OrderService;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ClientOrderController {
    private final OrderService orderService;

    // DTO for order creation
    public static class CreateOrderRequest {
        public Long clientId;
        public Long freelancerId;
        public String proposalTitle;
        public Double budget;
        public String status;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest req) {
        Order order = orderService.createOrderFromFrontend(
            req.clientId,
            req.freelancerId,
            req.proposalTitle,
            req.budget,
            req.status
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<Order>> getOrdersByFreelancer(@PathVariable Long freelancerId) {
        return ResponseEntity.ok(orderService.getOrdersByFreelancer(freelancerId));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Order>> getOrdersByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderService.getOrdersByClient(clientId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }
}
