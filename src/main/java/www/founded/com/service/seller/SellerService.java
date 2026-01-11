package www.founded.com.service.seller;

import www.founded.com.model.seller.Seller;

public interface SellerService {
    Seller getById(Long id);
    Seller getByName(String name);
}

