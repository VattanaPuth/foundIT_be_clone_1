package www.founded.com.service.seller.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.seller.GigSellerDTO;
import www.founded.com.dto.seller.ProductDTO;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Product;
import www.founded.com.model.seller.Seller;
import www.founded.com.repository.seller.GigSellerRepository;
import www.founded.com.repository.seller.ProductRepository;
import www.founded.com.repository.seller.SellerRepository;
import www.founded.com.service.seller.GigSellerService;

@Service
@RequiredArgsConstructor
public class GigSellerServiceImpl implements GigSellerService {

    private final GigSellerRepository gigSellerRepo;
    private final ProductRepository productRepo;
    private final AuthenticationService authenticationService;  // Assume this service fetches the logged-in seller

    @Override
    public GigSeller createGigSeller(GigSellerDTO gigSellerDTO) {
        // Retrieve the current logged-in seller using AuthenticationService or any other method
        Seller seller = authenticationService.getCurrentSeller();  // Assume it gets the logged-in Seller

        // If seller doesn't exist or the authentication is not correct
        if (seller == null) {
            throw new IllegalArgumentException("Seller not found or not authenticated");
        }

        // Alternatively, you can retrieve the seller using sellerRepo, if needed:
        // Seller seller = sellerRepo.findById(gigSellerDTO.getSellerId())
        //         .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        // Create GigSeller
        GigSeller gigSeller = new GigSeller();
        gigSeller.setSeller(seller);  // Now setting the seller using the retrieved seller
        gigSeller.setDescription(gigSellerDTO.getDescription());
        gigSeller.setServiceType(gigSellerDTO.getServiceType());
        gigSeller.setImageData(gigSellerDTO.getImageData());
        gigSeller.setPrice(gigSellerDTO.getPrice());
        gigSeller.setPublic(gigSellerDTO.isPublic());

        // Save GigSeller
        gigSeller = gigSellerRepo.save(gigSeller);

        // Create products if available in the DTO
        if (gigSellerDTO.getProducts() != null) {
            for (ProductDTO productDTO : gigSellerDTO.getProducts()) {
                Product product = new Product();
                product.setTitle(productDTO.getTitle());
                product.setDescription(productDTO.getDescription());
                product.setPrice(productDTO.getPrice());
                product.setAvailable(true);  // Mark the product as available
                product.setSeller(seller);  // Set the seller for the product

                // Associate the product with the GigSeller
                product.setGigSeller(gigSeller);
                productRepo.save(product);  // Save the product
            }
        }

        return gigSeller;
    }

    @Override
    public List<GigSeller> getPublicGigs() {
        return gigSellerRepo.findByIsPublic(true);
    }

    @Override
    public GigSeller getGigSellerById(Long id) {
        return gigSellerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gig Seller not found"));
    }

    @Override
    public void updateGigSeller(Long id, GigSellerDTO gigSellerDTO) {
        GigSeller gigSeller = getGigSellerById(id);
        gigSeller.setDescription(gigSellerDTO.getDescription());
        gigSeller.setServiceType(gigSellerDTO.getServiceType());
        gigSeller.setImageData(gigSellerDTO.getImageData());
        gigSeller.setPrice(gigSellerDTO.getPrice());
        gigSeller.setPublic(gigSellerDTO.isPublic());
        gigSellerRepo.save(gigSeller);
    }

    @Override
    public void deleteGigSeller(Long id) {
        GigSeller gigSeller = getGigSellerById(id);
        gigSellerRepo.delete(gigSeller);
    }
}
