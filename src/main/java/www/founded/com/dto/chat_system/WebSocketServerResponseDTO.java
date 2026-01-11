package www.founded.com.dto.chat_system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketServerResponseDTO <T>{
    private String type;
    private T payload;
}
