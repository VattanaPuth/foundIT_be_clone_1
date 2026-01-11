package www.founded.com.service.reset_password.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.model.register.UserRegister;
import www.founded.com.model.reset_password.PasswordResetToken;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.repository.reset_password.PasswordResetTokenRepository;
import www.founded.com.service.reset_password.PasswordResetService;
import www.founded.com.utils.otp.OtpGenerator;
import www.founded.com.utils.otp.OtpSender;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService{

	private final UserRegisterRepository userRegisterRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetTokenRepository tokenRepository;
    private final OtpSender otpSender;

	@Transactional
	@Override
	public void sendOtp(String email) {
		//save first to define endpoint of used (used is mean that user is not reset their password yet!) 
		//and
		//send: send the otp by email
		 Optional<UserRegister> optionalUser = userRegisterRepository.findByEmail(email);

	        if (optionalUser.isEmpty()) {
	            return;
	        }
	        
	        UserRegister user = optionalUser.get(); 

	        String otp = OtpGenerator.generateOtp(); 
	        String otpHash = passwordEncoder.encode(otp);
	        
	        PasswordResetToken token = new PasswordResetToken();
	        token.setUser(user);
	        token.setOtpHash(otpHash);
	        token.setExpiresAt(LocalDateTime.now().plusMinutes(2));
	        token.setUsed(false);

	        tokenRepository.save(token);

	        otpSender.sendOtpEmail(user.getEmail(), otp);
		}
	
	@Transactional
	@Override
	public boolean resendOtp(String email) {
		 Optional<UserRegister> optionalUser = userRegisterRepository.findByEmail(email);

	        if (optionalUser.isEmpty()) {
	            return false;
	        }
	        UserRegister user = optionalUser.get(); 
	        
	        Optional<PasswordResetToken> token = tokenRepository.findTopByUserAndUsedIsFalseOrderByIdDesc(user);

	        if (token.isPresent()) {	
	        	PasswordResetToken existingToken = token.get();
	        	
	        	if (existingToken.getExpiresAt() != null &&
	                    existingToken.getExpiresAt().isAfter(LocalDateTime.now())) {

	                    System.out.println("OTP is still valid. Cannot resend yet.");
	                    return false; 
	                }
	        }
	        
	        String newOtp = OtpGenerator.generateOtp(); 
	        String newOtpHash = passwordEncoder.encode(newOtp);
	        
	        PasswordResetToken newToken = new PasswordResetToken();
	        
	        newToken.setUser(user);
	        newToken.setOtpHash(newOtpHash);
	        newToken.setExpiresAt(LocalDateTime.now().plusMinutes(2));
	        
	        newToken.setUsed(false);

	        tokenRepository.save(newToken);

	        otpSender.sendOtpEmail(user.getEmail(), newOtp);
	     return true;
	}
	
	@Transactional 
	@Override
	public void resetPassword(String email, String otp, String newPassword) {
		UserRegister user = userRegisterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));

		// find the last id (its mean the last id sort by gp desc (desc: // 9 8 7))
        PasswordResetToken token = tokenRepository
                .findTopByUserAndUsedIsFalseOrderByIdDesc(user)
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));

        // expired token set time
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // if otp is not match otphash 
        if (!passwordEncoder.matches(otp, token.getOtpHash())) {
            throw new RuntimeException("Invalid email or OTP");
        }

        // after the client did change their password the token will update to set(true) in db
        token.setUsed(true);
        tokenRepository.save(token);

        // set new password + hashing
        user.setPassword(passwordEncoder.encode(newPassword));
        userRegisterRepository.save(user);
	}
}
