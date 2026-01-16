package www.founded.com.config.chat_system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import www.founded.com.service.chat_system.ChatMessageService;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ObjectMapper mapper;
    private final ChatMessageService chatService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocket(), "/chat")
                .setAllowedOrigins("http://localhost:3000",
                    "http://localhost:3001", 
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:3001"); //frontend url
    }

    @Bean
    public WebSocketHandler chatWebSocket() {
        return new ChatWebSocket(mapper, chatService);
    }
}

