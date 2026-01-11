package www.founded.com.service.seller;

import java.util.List;

import www.founded.com.dto.seller.ProductDTO;
import www.founded.com.model.seller.Product;

public interface ProductService {
    Product createProduct(Long sellerId, ProductDTO productDTO);
    List<Product> getAllProductsBySeller(Long sellerId);
    Product updateProductAvailability(Long productId, boolean isAvailable);
}
