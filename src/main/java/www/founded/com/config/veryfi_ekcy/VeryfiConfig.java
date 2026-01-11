package www.founded.com.config.veryfi_ekcy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class VeryfiConfig {
	
    @Value("${veryfi.anydocs-url}")
    private String anydocsUrl;

    @Bean
    public WebClient veryfiWebClient() {
        return WebClient.builder()
                .baseUrl(anydocsUrl)
                .build();
    }

}
