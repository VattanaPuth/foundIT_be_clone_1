package www.founded.com.utils.payment.aba;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PaywayCrypto {
    private PaywayCrypto() {}

    // PayWay docs show sha512 + API key, output looks base64 in purchase examples :contentReference[oaicite:6]{index=6}
    @SneakyThrows
    public static String hmacSha512Base64(String data, String apiKey) {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(raw);
    }

    // Refund "merchant_auth" is RSA encrypted using merchant public key :contentReference[oaicite:7]{index=7}
    @SneakyThrows
    public static String rsaEncryptBase64(String publicKeyPem, String plaintext) {
        PublicKey publicKey = parsePublicKey(publicKeyPem);
        var cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
        byte[] enc = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(enc);
    }

    @SneakyThrows
    private static PublicKey parsePublicKey(String pem) {
        String cleaned = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\n", "")
                .replace("\n", "")
                .trim();
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
