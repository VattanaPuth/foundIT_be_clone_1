package www.founded.com.dto.chat_system;

import lombok.Data;

@Data
public class WebSocketClientRequestDTO {
    private String type;
    private Object payload;
}
