package www.founded.com.model.reset_password;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.model.register.UserRegister;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@ManyToOne
	@JoinColumn(name = "user_pk_register_id", nullable = false)
    private UserRegister user;

    @Column(nullable = false)
    private String otpHash;
    
    private String newOtpHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;
}
