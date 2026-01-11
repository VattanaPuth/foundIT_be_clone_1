package www.founded.com.repository.seller;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Seller;

@Repository
public interface GigSellerRepository extends JpaRepository<GigSeller, Long> {
    List<GigSeller> findByIsPublic(boolean isPublic);  // Find public gigs
    List<GigSeller> findBySeller(Seller seller);
    List<GigSeller> findBySellerId_Id(Long sellerId);
}


