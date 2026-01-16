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
import www.founded.com.dto.chat_system.ProposalActionDTO;
import www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus;
import www.founded.com.model.chat_system.Message;
import www.founded.com.model.chat_system.Recipient;
import www.founded.com.model.chat_system.Sender;
import www.founded.com.model.payment.client_freelancer_contract.ContractOffer;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.chat_system.ChatMessageRepository;
import www.founded.com.repository.chat_system.RecipientRepository;
import www.founded.com.repository.chat_system.SenderRepository;
import www.founded.com.repository.client_freelancer_contract.ContractOfferRepository;
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
	private final ContractOfferRepository contractOfferRepository;

	@Transactional
	@Override
	public ChatMessageResponseDTO sendMessage(Long senderId, Long recipientId, ChatMessageRequestDTO messageRequest, MultipartFile file)  {
		if (senderId.equals(recipientId)) {
			System.out.println("[WARN] Attempted to send message to self. senderId=" + senderId);
			throw new IllegalArgumentException("Cannot send message to yourself.");
		}
		UserRegister senderUser = userRegisterRepository.findById(senderId)
			.orElseThrow(() -> new RuntimeException("Sender not found: " + senderId));
		UserRegister recipientUser = userRegisterRepository.findById(recipientId)
			.orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientId));

		Sender sender = new Sender();
		sender.setUser(senderUser);
		sender.setSenderName(senderUser.getUsername());

		Recipient recipient = new Recipient();
		recipient.setUser(recipientUser);
		recipient.setRecipientName(recipientUser.getUsername());

		Message message = new Message();
		message.setSenderId(sender);
		message.setSenderName(senderUser.getUsername());
		message.setRecipientId(recipient);
		message.setRecipientName(recipientUser.getUsername());
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
		// Set gigId if present in request
		message.setGigId(messageRequest.getGigId());

		Message saved = chatRepo.save(message);
		return toResponse(saved);
	}

	@Transactional
	@Override
	public ChatMessageResponseDTO toResponse(Message m) {
		ChatMessageResponseDTO messageResponse = new ChatMessageResponseDTO();
		messageResponse.setSenderName(m.getSenderName());
		messageResponse.setRecipientName(m.getRecipientName());
		messageResponse.setSenderId(m.getSenderId() != null && m.getSenderId().getUser() != null ? m.getSenderId().getUser().getId() : null);
		messageResponse.setRecipientId(m.getRecipientId() != null && m.getRecipientId().getUser() != null ? m.getRecipientId().getUser().getId() : null);
		messageResponse.setContents(m.getContents());
		messageResponse.setMessageType(m.getMessageType());
		messageResponse.setDay(LocalDate.now().getDayOfWeek());
		messageResponse.setTime(Time.valueOf(LocalTime.now()));
		messageResponse.setGigId(m.getGigId());
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

			// Save sender and recipient if new
			Sender savedSender = senderRepo.save(sender);
			Recipient savedRecipient = recipientRepo.save(recipient);
			Message saved = chatRepo.save(message);
			System.out.println("[DEBUG] sendMessage: saved messageId=" + saved.getId()
				+ ", senderId=" + savedSender.getUser().getId() + ", senderDbId=" + savedSender.getId()
				+ ", recipientId=" + savedRecipient.getUser().getId() + ", recipientDbId=" + savedRecipient.getId()
				+ ", contents='" + messageContent + "'");
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
		List<Message> messages = chatRepo.findMessagesBetweenUsers(user.getId(), otherUserId);
		System.out.println("[DEBUG] getMessagesBetweenUsers: userId=" + user.getId() + ", otherUserId=" + otherUserId + ", found messages=" + messages.size());
		return messages;
	}

	@Transactional
	@Override
	public void handleProposalAction(ProposalActionDTO action) {
		ContractOffer offer = contractOfferRepository.findById(action.getProposalId())
			.orElseThrow(() -> new RuntimeException("Proposal not found: " + action.getProposalId()));

		if ("accept".equals(action.getAction())) {
			offer.setStatus(ContractOfferStatus.ACCEPTED);
			offer.setAcceptedAt(java.time.Instant.now());
		} else if ("decline".equals(action.getAction())) {
			offer.setStatus(ContractOfferStatus.REJECTED);
			offer.setRejectedAt(java.time.Instant.now());
		} else {
			throw new IllegalArgumentException("Invalid action: " + action.getAction());
		}

		contractOfferRepository.save(offer);
		System.out.println("[DEBUG] handleProposalAction: proposalId=" + action.getProposalId() + ", action=" + action.getAction() + ", status=" + offer.getStatus());
	}
}
