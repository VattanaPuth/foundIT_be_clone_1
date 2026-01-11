package www.founded.com.service.chat_system;

import org.springframework.web.multipart.MultipartFile;

import www.founded.com.dto.chat_system.ChatMessageRequestDTO;
import www.founded.com.dto.chat_system.ChatMessageResponseDTO;
import www.founded.com.model.chat_system.Message;

public interface ChatMessageService {
	ChatMessageResponseDTO sendMessage(String senderId, String recipientId , ChatMessageRequestDTO messageRequest, MultipartFile file);
	ChatMessageResponseDTO toResponse(Message m);
	Message sendMessage(Long senderId, Long recipientId, String messageContent);
	void markMessageAsRead(Long messageId);
}
