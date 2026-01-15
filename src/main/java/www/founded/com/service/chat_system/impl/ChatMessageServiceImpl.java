package www.founded.com.service.chat_system.impl;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
		
		UserRegister senderUserId = userRegisterRepository.findByUsername(senderUsername)
			.orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));
	    UserRegister recipientUserId = userRegisterRepository.findByUsername(messageRequest.getRecipientName())
	    	.orElseThrow(() -> new RuntimeException("Recipient not found: " + messageRequest.getRecipientName()));
		
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
		message.setMessageType(messageRequest.getMessageType());
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
		messageResponse.setMessageType(m.getMessageType());
		messageResponse.setDay(LocalDate.now().getDayOfWeek());
		messageResponse.setTime(Time.valueOf(LocalTime.now()));
		
	    if (m != null && m.getFileData() != null && m.getFileData().length > 0) {
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
		return sendMessage(senderId, recipientId, messageContent, "text");
	}

	@Transactional
	public Message sendMessage(Long senderId, Long recipientId, String messageContent, String messageType) {
		UserRegister senderUser = userRegisterRepository.findById(senderId)
			.orElseThrow(() -> new IllegalArgumentException("Sender not found"));
		UserRegister recipientUser = userRegisterRepository.findById(recipientId)
			.orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

		Sender sender = senderRepo.findByUser(senderUser)
			.orElseGet(() -> {
				Sender newSender = new Sender();
				newSender.setUser(senderUser);
				newSender.setSenderName(senderUser.getUsername());
				newSender.setRole(senderUser.getRole());
				newSender.setEmail(senderUser.getEmail());
				return senderRepo.save(newSender);
			});

		Recipient recipient = recipientRepo.findByUser(recipientUser)
			.orElseGet(() -> {
				Recipient newRecipient = new Recipient();
				newRecipient.setUser(recipientUser);
				newRecipient.setRecipientName(recipientUser.getUsername());
				newRecipient.setRole(recipientUser.getRole());
				newRecipient.setEmail(recipientUser.getEmail());
				return recipientRepo.save(newRecipient);
			});

	        Message message = new Message();
	        message.setSenderId(sender);
	        message.setSenderName(sender.getSenderName());
	        message.setRecipientId(recipient);
	        message.setRecipientName(recipient.getRecipientName());
	        message.setContents(messageContent);
	        message.setMessageType(messageType);
	        message.setDay(LocalDate.now().getDayOfWeek());
	        message.setTime(Time.valueOf(LocalTime.now()));
	        message.setFileData(null);
	        message.setFileName(null);
	        message.setFileType(null);
	        message.setRead(false);

	        Message saved = chatRepo.save(message);

	        // Trigger the notification once the message is sent
	        notificationService.sendMessageNotification(sender, recipient, messageContent);
	    return saved;    
	}

	@Transactional
	@Override
	public void markMessageAsRead(Long messageId) {
		 Message message = chatRepo.findById(messageId)
	                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

	        message.setRead(true);
	        chatRepo.save(message);
	    }

	@Override
	public List<Message> getUserConversations(String userEmailOrUsername) {
		UserRegister user = userRegisterRepository.findByUsernameOrEmail(userEmailOrUsername)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return chatRepo.findConversationsByUserId(user.getId());
	}

	@Override
	public List<Message> getMessagesBetweenUsers(String userEmailOrUsername, Long otherUserId) {
		UserRegister user = userRegisterRepository.findByUsernameOrEmail(userEmailOrUsername)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return chatRepo.findMessagesBetweenUsers(user.getId(), otherUserId);
	}
}
