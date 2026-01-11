package www.founded.com.jwt;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class JwtSecretGeneratorTest {

    @Test
    void generateJwtSecret() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Secret = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Your jwt.secret value:");
        System.out.println(base64Secret);
    }
}

