package www.founded.com.service.reset_password;

public interface PasswordResetService {
	void sendOtp(String email);
	boolean resendOtp(String email);
	void resetPassword(String email, String otp, String newPassword);
}
