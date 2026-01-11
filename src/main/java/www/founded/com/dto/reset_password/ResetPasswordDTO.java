package www.founded.com.dto.reset_password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
	@NotBlank @Email
    private String email;

    @NotBlank
    private String otp;

    @NotBlank
    @Size(min = 6, max = 64)
    private String newPassword;
}
