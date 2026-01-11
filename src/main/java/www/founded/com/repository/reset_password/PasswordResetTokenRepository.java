package www.founded.com.repository.reset_password;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import www.founded.com.model.register.UserRegister;
import www.founded.com.model.reset_password.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByUserAndUsedIsFalseOrderByIdDesc(UserRegister user);
}

