package www.founded.com.service.chat_system.impl;

import java.io.IOException;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.chat_system.ChatMessageRequestDTO;
import www.founded.com.dto.chat_system.ChatMessageResponseDTO;
import www.founded.com.model.chat_system.Message;
import www.founded.com.model.chat_system.Recipient;
import www.founded.com.model.chat_system.Sender;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.chat_system.ChatMessageRepository;
import www.founded.com.repository.chat_system.RecipientRepository;
import www.founded.com.repository.chat_system.SenderRepository;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.service.chat_system.ChatMessageService;
import www.founded.com.service.notification.NotificationService;

@Service
@Data
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
	
	private final ChatMessageRepository chatRepo;
	private final UserRegisterRepository userRegisterRepository;
	private final NotificationService notificationService;
	private final SenderRepository senderRepo;
	private final RecipientRepository recipientRepo;

	@Transactional
	@Override
	public ChatMessageResponseDTO sendMessage(String senderUsername, String recipientName, ChatMessageRequestDTO messageRequest, MultipartFile file)  {
		
		UserRegister senderUserId = userRegisterRepository.findByUsername(senderUsername);
	    UserRegister recipientUserId = userRegisterRepository.findByUsername(messageRequest.getRecipientName());
		
	    Sender sender = new Sender();
	    sender.setUser(senderUserId);
	    sender.setSenderName(senderUsername);
	    
	    Recipient recipient = new Recipient();
	    recipient.setUser(recipientUserId);
	    recipient.setRecipientName(recipientName);
	    
		Message message = new Message();
		message.setSenderId(sender);	
		message.setSenderName(senderUsername);
		message.setRecipientId(recipient);
		message.setRecipientName(recipientName);
		message.setContents(messageRequest.getContents());
		message.setDay(LocalDate.now().getDayOfWeek()); 
		message.setTime(Time.valueOf(LocalTime.now())); 
     	message.setFileName(file != null ? file.getOriginalFilename() : null);
	    message.setFileType(file != null ? file.getContentType() : null);
	    try {
			message.setFileData(file != null ? file.getBytes() : null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Message saved = chatRepo.save(message);
		return toResponse(saved);
	}

	@Transactional
	@Override
	public ChatMessageResponseDTO toResponse(Message m) {
		ChatMessageResponseDTO messageResponse = new ChatMessageResponseDTO();
		messageResponse.setSenderName(m.getSenderName());
		messageResponse.setRecipientName(m.getRecipientName());
		messageResponse.setContents(m.getContents());
		messageResponse.setDay(LocalDate.now().getDayOfWeek());
		messageResponse.setTime(Time.valueOf(LocalTime.now()));
		
	    if (m != null && m.getFileData().length > 0) {
	    	messageResponse.setFileName(m.getFileName());
	    	messageResponse.setFileType(m.getFileType());
	        messageResponse.setFileData(m.getFileData());
	    } else {
	    	messageResponse.setFileData(null);
	    	messageResponse.setFileName(null);
	    	messageResponse.setFileType(null);
	    }
		
		return messageResponse;
	}

	@Transactional
	@Override
	public Message sendMessage(Long senderId, Long recipientId, String messageContent) {
		 Sender sender = senderRepo.findById(senderId)
	                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

	     Recipient recipient = recipientRepo.findById(recipientId)
	                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

	        Message message = new Message();
	        message.setSenderId(sender);
	        message.setRecipientId(recipient);
	        message.setContents(messageContent);
	        message.setDay(LocalDate.now().getDayOfWeek());
	        message.setTime(Time.valueOf(LocalTime.now()));

	        chatRepo.save(message);

	        // Trigger the notification once the message is sent
	        notificationService.sendMessageNotification(sender, recipient, messageContent);
	    return message;    
	}

	@Transactional
	@Override
	public void markMessageAsRead(Long messageId) {
		 Message message = chatRepo.findById(messageId)
	                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

	        message.setRead(true);
	        chatRepo.save(message);
	    }
}
