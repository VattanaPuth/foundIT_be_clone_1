package www.founded.com.config.aba_payway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Configuration
@Getter
public class AbaPaywayConfig {
    @Value("${aba.payway.merchantId}")
    private String merchantId;
    
    @Value("${aba.payway.publicKeyPem}")
    private String publicKeyPem;

    @Value("${aba.payway.apiKey}")
    private String apiKey;
	
    @Value("${aba.payway.api.endpoint}")
    private String apiEndpoint;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
