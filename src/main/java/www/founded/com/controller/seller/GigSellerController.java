package www.founded.com.controller.seller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.seller.GigSellerDTO;
import www.founded.com.dto.seller.ProductDTO;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Product;
import www.founded.com.service.seller.GigSellerService;
import www.founded.com.service.seller.ProductService;

@RestController
@RequestMapping("/gigs/seller")
@RequiredArgsConstructor
public class GigSellerController {

    private final GigSellerService gigSellerService;
    private final ProductService productService;

    // Create a new gig (Seller creates a gig)
    @PostMapping("/create")
    public ResponseEntity<GigSeller> createGig(@RequestBody GigSellerDTO gigSellerDTO) {
        return ResponseEntity.ok(gigSellerService.createGigSeller(gigSellerDTO));
    }

    // Get all public gigs
    @GetMapping("/public")
    public ResponseEntity<List<GigSeller>> getPublicGigs() {
        return ResponseEntity.ok(gigSellerService.getPublicGigs());
    }

    // Get a specific gig by ID
    @GetMapping("/{id}")
    public ResponseEntity<GigSeller> getGigById(@PathVariable Long id) {
        return ResponseEntity.ok(gigSellerService.getGigSellerById(id));
    }

    // Update gig details
    @PutMapping("/{id}")
    public ResponseEntity<GigSeller> updateGig(@PathVariable Long id, @RequestBody GigSellerDTO gigSellerDTO) {
        gigSellerService.updateGigSeller(id, gigSellerDTO);
        return ResponseEntity.ok(gigSellerService.getGigSellerById(id));
    }

    // Delete gig
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGig(@PathVariable Long id) {
        gigSellerService.deleteGigSeller(id);
        return ResponseEntity.ok().build();
    }

    // Create a product for the gig
    @PostMapping("/{id}/product")
    public ResponseEntity<Product> createProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.createProduct(id, productDTO));
    }

    // Get all products by Seller
    @GetMapping("/{id}/products")
    public ResponseEntity<List<Product>> getProductsBySeller(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getAllProductsBySeller(id));
    }

    // Update availability of a product
    @PutMapping("/{id}/product/availability")
    public ResponseEntity<Product> updateProductAvailability(@PathVariable Long id, @RequestParam boolean isAvailable) {
        return ResponseEntity.ok(productService.updateProductAvailability(id, isAvailable));
    }
}
