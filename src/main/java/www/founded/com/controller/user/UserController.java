package www.founded.com.controller.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.user.UpdateRoleRequestDTO;
import www.founded.com.service.user.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/update-role")
    public ResponseEntity<?> updateUserRole(
            @RequestBody UpdateRoleRequestDTO request,
            Authentication authentication) {
        
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "message", "User not authenticated"
            ));
        }

        String username = authentication.getName();
        
        try {
            userService.updateUserRole(username, request.getRole());
            
            return ResponseEntity.ok(Map.of(
                "message", "Role updated successfully",
                "role", request.getRole(),
                "username", username
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Role update failed",
                "message", e.getMessage()
            ));
        }
    }
}
