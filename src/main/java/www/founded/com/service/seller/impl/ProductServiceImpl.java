package www.founded.com.service.seller.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.seller.ProductDTO;
import www.founded.com.model.seller.Product;
import www.founded.com.model.seller.Seller;
import www.founded.com.repository.seller.ProductRepository;
import www.founded.com.repository.seller.SellerRepository;
import www.founded.com.service.seller.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final SellerRepository sellerRepo;

    @Override
    public Product createProduct(Long sellerId, ProductDTO productDTO) {
        Seller seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        // Create Product
        Product product = new Product();
        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setAvailable(true);  // Mark the product as available
        product.setSeller(seller);  // Set the seller for the product

        return productRepo.save(product);
    }

    @Override
    public List<Product> getAllProductsBySeller(Long sellerId) {
        Seller seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        return productRepo.findBySeller(seller);
    }

    @Override
    public Product updateProductAvailability(Long productId, boolean isAvailable) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setAvailable(isAvailable);
        return productRepo.save(product);
    }
}
