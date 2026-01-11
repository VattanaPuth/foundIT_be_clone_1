package www.founded.com.utils.security;

import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class KeyUtils {
	static String secretKey = "4tj9TyBxEymIXhVUsIUzdVrrcY6RYQj7Bt8LCUeSAEM=";
	
	public static SecretKey getKey() {
		byte[] secretKeyByte = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(secretKeyByte);
	}
}
