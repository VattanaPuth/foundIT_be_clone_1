package www.founded.com.service.seller;

import java.util.List;

import www.founded.com.dto.seller.GigSellerDTO;
import www.founded.com.dto.seller.ProductDTO;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Product;

public interface GigSellerService {
    GigSeller createGigSeller(GigSellerDTO gigSellerDTO);
    List<GigSeller> getPublicGigs();
    GigSeller getGigSellerById(Long id);
    void updateGigSeller(Long id, GigSellerDTO gigSellerDTO);
    void deleteGigSeller(Long id);
}

