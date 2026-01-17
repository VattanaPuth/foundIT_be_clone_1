package www.founded.com.service.chat_system;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import www.founded.com.dto.chat_system.ChatMessageRequestDTO;
import www.founded.com.dto.chat_system.ChatMessageResponseDTO;
import www.founded.com.dto.chat_system.ProposalActionDTO;
import www.founded.com.model.chat_system.Message;

public interface ChatMessageService {
        www.founded.com.model.payment.client_freelancer_contract.ContractOffer saveContractOffer(www.founded.com.model.payment.client_freelancer_contract.ContractOffer offer);

        void sendProposalMessage(Long senderId, Long recipientId, String messageContent, Long contractOfferId);
    ChatMessageResponseDTO sendMessage(Long senderId, Long recipientId, ChatMessageRequestDTO messageRequest, MultipartFile file);
    ChatMessageResponseDTO toResponse(Message m);
    Message sendMessage(Long senderId, Long recipientId, String messageContent);
    Message sendMessage(Long senderId, Long recipientId, String messageContent, String messageType);
    void markMessageAsRead(Long messageId);
    List<Message> getUserConversations(String userEmail);
    List<Message> getMessagesBetweenUsers(String userEmail, Long otherUserId);
    void handleProposalAction(ProposalActionDTO action);
}
