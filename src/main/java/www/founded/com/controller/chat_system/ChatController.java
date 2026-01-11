package www.founded.com.controller.chat_system;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.chat_system.ChatMessageRequestDTO;
import www.founded.com.dto.chat_system.ChatMessageResponseDTO;
import www.founded.com.service.chat_system.ChatMessageService;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;

    @PostMapping("/sendMessage")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @RequestParam("senderName") String senderName,  // Use String for sender
            @RequestParam("recipientName") String recipientName,  // Use String for recipient
            @RequestParam("contents") String contents,
            @RequestParam(value = "file", required = false) MultipartFile file) {  // File is optional

        // Create the ChatMessageRequestDTO with the text data
        ChatMessageRequestDTO messageRequest = new ChatMessageRequestDTO();
        messageRequest.setSenderName(senderName);  // Set sender name
        messageRequest.setRecipientName(recipientName);  // Set recipient name
        messageRequest.setContents(contents);  // Set message contents

        // Check if file is uploaded
        if (file != null && !file.isEmpty()) {
            // Handle file data if provided
            try {
                messageRequest.setFileData(file.getBytes());
                messageRequest.setFileName(file.getOriginalFilename());
                messageRequest.setFileType(file.getContentType());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

        // Call the service method to handle sending the message
        ChatMessageResponseDTO response = chatMessageService.sendMessage(senderName, recipientName, messageRequest, file);

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
}
