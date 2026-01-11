package www.founded.com.controller.veryfi_ekyc;


import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.veryfi_ekyc.EkycRequestDTO;
import www.founded.com.dto.veryfi_ekyc.EkycResponseDTO;
import www.founded.com.service.veryfi_ekyc.impl.EkycServiceImpl;

@RestController
@RequestMapping("/ekyc")
@RequiredArgsConstructor
public class EkycController {

    private final EkycServiceImpl ekycService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public EkycResponseDTO verify(@RequestParam("file") MultipartFile file, @RequestParam("metadata") String metadataJson, Authentication authentication) throws Exception {
        System.out.println("DEBUG - EkycController.verify called");
        System.out.println("DEBUG - Authentication object: " + authentication);
        System.out.println("DEBUG - Authentication.getName(): " + (authentication != null ? authentication.getName() : "NULL"));
        
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("User not authenticated. Please log in again.");
        }
        
        EkycRequestDTO request = objectMapper.readValue(metadataJson, EkycRequestDTO.class);
        String username = authentication.getName();
        System.out.println("DEBUG - Extracted username: " + username);
        return ekycService.verifyIdDocument(file, request, username);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(Authentication authentication) {
        System.out.println("DEBUG - EkycController.getVerificationStatus called");
        System.out.println("DEBUG - Authentication object: " + authentication);
        System.out.println("DEBUG - Authentication.getName(): " + (authentication != null ? authentication.getName() : "NULL"));
        
        if (authentication == null || authentication.getName() == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("verified", false);
            response.put("error", "Not authenticated");
            return ResponseEntity.ok(response);
        }
        
        String username = authentication.getName();
        System.out.println("DEBUG - Checking verification status for username: " + username);
        boolean isVerified = ekycService.isUserVerified(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("verified", isVerified);
        
        return ResponseEntity.ok(response);
    }
}

