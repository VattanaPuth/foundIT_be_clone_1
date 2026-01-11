package www.founded.com.utils.otp;

import java.security.SecureRandom;

public class OtpGenerator {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	public static String generateOtp() {
		int otp = 100_000 + SECURE_RANDOM.nextInt(900_000);
        return String.valueOf(otp);
	}
}
