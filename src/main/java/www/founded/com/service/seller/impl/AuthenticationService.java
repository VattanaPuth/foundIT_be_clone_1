package www.founded.com.service.seller.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import www.founded.com.model.seller.Seller;
import www.founded.com.repository.seller.SellerRepository;

@Service
public class AuthenticationService {

    private final SellerRepository sellerRepository;

    public AuthenticationService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public Seller getCurrentSeller() {
        // Get the username of the currently authenticated user
        String username = getAuthenticatedUsername();

        // Retrieve the Seller based on the username
        return sellerRepository.findByName(username);
    }

    private String getAuthenticatedUsername() {
        // Get the currently authenticated user’s username
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}
