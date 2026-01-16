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

        String contents = "Proposal: " + coverLetter + " | Rate: " + rate + " | Delivery: " + deliveryTime + " days";

    	chatMessageService.sendMessage(senderId, recipientId, contents, "proposal");
        return ResponseEntity.ok("Proposal sent successfully.");
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Message>> getUserConversations(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Message> conversations = chatMessageService.getUserConversations(userEmail);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessagesBetweenUsers(
            @RequestParam Long otherUserId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<Message> messages = chatMessageService.getMessagesBetweenUsers(userEmail, otherUserId);
        return ResponseEntity.ok(messages);
    }
}
