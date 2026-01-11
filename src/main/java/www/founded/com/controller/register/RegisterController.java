package www.founded.com.controller.register;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.register.UserRegisterRequestDTO;
import www.founded.com.dto.reset_password.ForgotPasswordDTO;
import www.founded.com.dto.reset_password.ResendOtp;
import www.founded.com.dto.reset_password.ResetPasswordDTO;
import www.founded.com.mapper.UserRegisterMapper;
import www.founded.com.model.register.UserRegister;
import www.founded.com.service.register.impl.UserRegisterServiceImpl;
import www.founded.com.service.reset_password.PasswordResetService;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {
	private final UserRegisterMapper urf;
	private final UserRegisterServiceImpl urs;
	private final PasswordResetService passwordResetService;

	@PostMapping
	public ResponseEntity<?> register(@RequestBody UserRegisterRequestDTO userRegisterDTO){
		UserRegister registerUser = urf.toUserRegisterRequest(userRegisterDTO);
		registerUser = urs.saveUser(registerUser);
		return ResponseEntity.ok(urf.toUserRegisterResDTO(registerUser));
	}
	
	@PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordDTO request) {
        passwordResetService.sendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "An OTP has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO request) {
        passwordResetService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
    
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody @Valid ResendOtp request) {
    	boolean resent = passwordResetService.resendOtp(request.getEmail());
    	
    	if (!resent) {
            return ResponseEntity
                .badRequest()
                .body("OTP is still valid. Cannot resend yet.");
        }

        return ResponseEntity.ok(Map.of("message", "New OTP sent"));
    }
}
