package www.founded.com.dto.reset_password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
	@NotBlank
    @Email
    private String email;
}
