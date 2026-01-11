package www.founded.com.service.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.security.Role;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.register.UserRegisterRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRegisterRepository userRegisterRepository;

    @Transactional
    public void updateUserRole(String usernameOrEmail, String roleName) {
        // Try to find user by username first
        UserRegister user = userRegisterRepository.findByUsername(usernameOrEmail);
        
        // If not found by username, try by email
        if (user == null) {
            user = userRegisterRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + usernameOrEmail));
        }

        // Validate role name
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new RuntimeException("Role name cannot be empty");
        }

        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
            userRegisterRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleName + ". Valid roles are: CLIENT, FREELANCER, SELLER, ADMIN");
        }
    }
}
