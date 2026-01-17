package www.founded.com.controller.chat_system;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.chat_system.ChatMessageRequestDTO;
import www.founded.com.dto.chat_system.ChatMessageResponseDTO;
import www.founded.com.model.chat_system.Message;
import www.founded.com.service.chat_system.ChatMessageService;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final www.founded.com.repository.client.ClientRepository clientRepository;
    private final www.founded.com.repository.freelancer.FreelancerRepository freelancerRepository;

    @PostMapping("/sendMessage")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @RequestParam("senderId") Long senderId,
            @RequestParam("recipientId") Long recipientId,
            @RequestParam("contents") String contents,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ChatMessageRequestDTO messageRequest = new ChatMessageRequestDTO();
        messageRequest.setSenderName(String.valueOf(senderId));
        messageRequest.setRecipientName(String.valueOf(recipientId));
        messageRequest.setContents(contents);

        if (file != null && !file.isEmpty()) {
            try {
                messageRequest.setFileData(file.getBytes());
                messageRequest.setFileName(file.getOriginalFilename());
                messageRequest.setFileType(file.getContentType());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

        ChatMessageResponseDTO response = chatMessageService.sendMessage(senderId, recipientId, messageRequest, file);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/p2p")
    public ResponseEntity<String> sendMessage(
            @RequestParam Long senderId, 
            @RequestParam Long recipientId,
            @RequestParam String messageContent) {
        
    	chatMessageService.sendMessage(senderId, recipientId, messageContent);
        return ResponseEntity.ok("Message sent successfully.");
    }

    @PostMapping("/sendProposal")
    public ResponseEntity<String> sendProposal(
            @RequestParam Long senderId,
            @RequestParam Long recipientId,
            @RequestParam String coverLetter,
            @RequestParam String rate,
            @RequestParam String deliveryTime) {

        // Create ContractOffer entity
        // Fetch client and freelancer entities from DB
        www.founded.com.model.client.Client client = clientRepository.findByUser_Id(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found for userRegisterId=" + senderId));
        www.founded.com.model.freelancer.Freelancer freelancer = freelancerRepository.findByUser_Id(recipientId)
            .orElseThrow(() -> new IllegalArgumentException("Freelancer not found for userRegisterId=" + recipientId));

        www.founded.com.model.payment.client_freelancer_contract.ContractOffer offer = new www.founded.com.model.payment.client_freelancer_contract.ContractOffer();
        offer.setClientId(client);
        offer.setFreelancerId(freelancer);
        offer.setTitle("Proposal for gig/job");
        offer.setDescription(coverLetter);
        offer.setTotalBudget(new java.math.BigDecimal(rate));
        offer.setDeliveryDays(Integer.parseInt(deliveryTime));
        offer.setMessage(coverLetter);
        offer.setStatus(www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus.SENT);
        offer.setIsPublic(false);
        // Save offer
        offer = chatMessageService.saveContractOffer(offer);
        System.out.println("[DEBUG] Created ContractOffer with ID: " + offer.getId());

        String contents = "Proposal: " + coverLetter + " | Rate: " + rate + " | Delivery: " + deliveryTime + " days";
        chatMessageService.sendProposalMessage(senderId, recipientId, contents, offer.getId());
        return ResponseEntity.ok("Proposal sent successfully. ContractOfferId=" + offer.getId());
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ChatMessageResponseDTO>> getUserConversations(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Message> conversations = chatMessageService.getUserConversations(userEmail);
        List<ChatMessageResponseDTO> response = conversations.stream().map(chatMessageService::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponseDTO>> getMessagesBetweenUsers(
            @RequestParam Long otherUserId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<Message> messages = chatMessageService.getMessagesBetweenUsers(userEmail, otherUserId);
        List<ChatMessageResponseDTO> response = messages.stream().map(chatMessageService::toResponse).toList();
        return ResponseEntity.ok(response);
    }
}
