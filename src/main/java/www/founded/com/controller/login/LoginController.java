package www.founded.com.controller.login;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.login.UserLoginEmailRequestDTO;
import www.founded.com.dto.login.UserLoginRequestDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.register.UserRegister;
import www.founded.com.model.seller.Seller;
import www.founded.com.repository.client.ClientRepository;
import www.founded.com.repository.freelancer.FreelancerRepository;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.repository.seller.SellerRepository;
import www.founded.com.utils.security.KeyUtils;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private final UserRegisterRepository userRegisterRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final SellerRepository sellerRepository;
    private final FreelancerRepository freelancerRepository;
    
    @PostMapping("/login/email")
    public ResponseEntity<?> loginWithEmail(@RequestBody UserLoginEmailRequestDTO loginRequest) {
        try {
            // Find user by email
            UserRegister user = userRegisterRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));
            
            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            
            // Save to role table if not exists
            switch (user.getRole().name()) {
                case "CLIENT":
                    if (clientRepository.findByUser_Id(user.getId()).isEmpty()) {
                        Client client = new Client();
                        client.setUser(user);
                        client.setName(user.getUsername());
                        clientRepository.save(client);
                        logger.info("Saved CLIENT to client_user table: userId={}", user.getId());
                    }
                    break;
                case "SELLER":
                    if (sellerRepository.findByUser_Id(user.getId()).isEmpty()) {
                        Seller seller = new Seller();
                        seller.setUser(user);
                        seller.setName(user.getUsername());
                        seller.setPublic(true);
                        sellerRepository.save(seller);
                        logger.info("Saved SELLER to seller_user table: userId={}", user.getId());
                    }
                    break;
                case "FREELANCER":
                    logger.info("Login as FREELANCER: userId={}, username={}", user.getId(), user.getUsername());
                    if (freelancerRepository.findByUser_Id(user.getId()).isEmpty()) {
                        Freelancer freelancer = new Freelancer();
                        freelancer.setUser(user);
                        freelancer.setName(user.getUsername());
                        freelancerRepository.save(freelancer);
                        logger.info("Saved FREELANCER to freelancer_user table: userId={}", user.getId());
                    } else {
                        logger.info("FREELANCER already exists in freelancer_user table: userId={}", user.getId());
                    }
                    break;
            }
            
            // Build authorities list from user's role
            var authorities = user.getRole().getAuthorities().stream()
                    .map(authority -> Map.of("authority", authority.getAuthority()))
                    .collect(Collectors.toList());
            
            // Generate JWT token
            String token = Jwts.builder()
                    .setSubject(user.getEmail())
                    .setIssuedAt(new Date())
                    .claim("authorities", authorities)
                    .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(7)))
                    .setIssuer("FoundIT")
                    .signWith(KeyUtils.getKey())
                    .compact();
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful with email");
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name()
            ));
            
            // Return response with token in header
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(response);
            
        } catch (Exception e) {
            logger.error("Login error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO loginRequest) {
        try {
            // Try to find user by username or email
            UserRegister user = userRegisterRepository.findByUsername(loginRequest.getUsername())
                .orElseGet(() -> userRegisterRepository.findByEmail(loginRequest.getUsername())
                        .orElseThrow(() -> new RuntimeException("Invalid username/email or password")));
            
            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid username/email or password");
            }
            
            // Save to role table if not exists
            switch (user.getRole().name()) {
                case "CLIENT":
                    if (clientRepository.findByUser_Id(user.getId()).isEmpty()) {
                        Client client = new Client();
                        client.setUser(user);
                        client.setName(user.getUsername());
                        clientRepository.save(client);
                        logger.info("Saved CLIENT to client_user table: userId={}", user.getId());
                    }
                    break;
                case "SELLER":
                    if (sellerRepository.findByUser_Id(user.getId()).isEmpty()) {
                        Seller seller = new Seller();
                        seller.setUser(user);
                        seller.setName(user.getUsername());
                        seller.setPublic(true);
                        sellerRepository.save(seller);
                        logger.info("Saved SELLER to seller_user table: userId={}", user.getId());
                    }
                    break;
                case "FREELANCER":
                    logger.info("Login as FREELANCER: userId={}, username={}", user.getId(), user.getUsername());
                    if (freelancerRepository.findByUser_Id(user.getId()).isEmpty()) {
                        Freelancer freelancer = new Freelancer();
                        freelancer.setUser(user);
                        freelancer.setName(user.getUsername());
                        freelancerRepository.save(freelancer);
                        logger.info("Saved FREELANCER to freelancer_user table: userId={}", user.getId());
                    } else {
                        logger.info("FREELANCER already exists in freelancer_user table: userId={}", user.getId());
                    }
                    break;
            }
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Login error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    /**
     * Check authentication status
     */
    @PostMapping("/check-auth")
    public ResponseEntity<?> checkAuth(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Get user details from database
            String username = authentication.getName();
            UserRegister user = userRegisterRepository.findByEmail(username)
                .orElseGet(() -> userRegisterRepository.findByUsername(username).orElse(null));
            
            if (user != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("role", user.getRole().name());
                response.put("authorities", authentication.getAuthorities());
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", false);
        return ResponseEntity.ok(response);
    }
}
