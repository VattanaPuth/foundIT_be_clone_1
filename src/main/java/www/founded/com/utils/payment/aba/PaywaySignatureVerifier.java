package www.founded.com.utils.payment.aba;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PaywaySignatureVerifier {

    @Value("${aba.payway.apiKey}")
    private String apiKey; // this is PRIVATE KEY

    /**
     * Verify PayWay pushback hash
     */
    public boolean verifyPushback(
            String tranId,
            String apv,
            String status,
            String receivedHash
    ) {
        if (tranId == null || apv == null || status == null || receivedHash == null) {
            return false;
        }

        try {
            // IMPORTANT: order must match PayWay documentation
            String raw = tranId + apv + status + apiKey;

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            String computedHash = Base64.getEncoder().encodeToString(digest);

            return computedHash.equals(receivedHash);

        } catch (Exception e) {
            return false;
        }
    }
}