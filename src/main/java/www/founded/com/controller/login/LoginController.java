package www.founded.com.controller.login;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.utils.security.KeyUtils;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    
    private final UserRegisterRepository userRegisterRepository;
    private final PasswordEncoder passwordEncoder;
    
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
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name()
            ));
            
            // Return response with token in header
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(response);
            
        } catch (Exception e) {
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
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", Map.of(
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
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
