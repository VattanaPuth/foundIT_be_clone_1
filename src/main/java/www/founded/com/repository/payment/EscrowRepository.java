package www.founded.com.repository.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.payment.Escrow;
import www.founded.com.model.seller.Order;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Long> {
    Optional<Escrow> findById(Long id);
    Optional<Escrow> findByPaywayTxnId(String paywayTxnId);
    Optional<Escrow> findByOrder(Order order);
}
