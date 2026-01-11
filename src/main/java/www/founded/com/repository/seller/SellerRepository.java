package www.founded.com.repository.seller;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.seller.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Seller getById(Long id);
    Seller findByName(String name);
    Optional<Seller> findByUser_Id(Long userRegisterId);
}
