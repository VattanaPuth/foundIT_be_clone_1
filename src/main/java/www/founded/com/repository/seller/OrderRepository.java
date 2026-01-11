package www.founded.com.repository.seller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.enum_.seller.OrderStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);
    List<Order> findByClient(Client client);
    List<Order> findByClient_Id(Long clientId);
    List<Order> findByGigSeller(GigSeller gigSeller);
    List<Order> findByGigSellerId(Long sellerId);
    Optional<Order> findByOrderIdAndStatus(Long id, OrderStatus status);
    List<Order> findByFreelancer(Freelancer freelancer);
}
