package www.founded.com.service.veryfi_ekyc.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import www.founded.com.dto.veryfi_ekyc.EkycRequestDTO;
import www.founded.com.dto.veryfi_ekyc.EkycResponseDTO;
import www.founded.com.enum_.veryfi_ekyc.IdDocType;
import www.founded.com.service.veryfi_ekyc.EkycService;

@Service
@RequiredArgsConstructor
public class EkycServiceImpl implements EkycService {
	
	private final WebClient veryfiWebClient;
	private final ObjectMapper objectMapper;
	
    @Value("${veryfi.client-id}")
    private String clientId;

    @Value("${veryfi.username}")
    private String username;

    @Value("${veryfi.api-key}")
    private String apiKey;
    
    @Value("${veryfi.client-secret}")
    private String clientSecret;
    
    @Value("${veryfi.blueprint.id-card}")
    private String idCardBlueprintName;
    
    @Value("${veryfi.blueprint.passport}")
    private String passportBlueprintName;

    @Value("${veryfi.blueprint.driver-license}")
    private String driverLicenseBlueprintName;
	
	@Override
	public EkycResponseDTO verifyIdDocument(MultipartFile file, EkycRequestDTO request) {
        
		EkycResponseDTO res = new EkycResponseDTO();
		
        try {
            String fileData = Base64.getEncoder().encodeToString(file.getBytes());

            String blueprintName = resolveBlueprintName(request.getDocType());
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("file_data", fileData);
            payload.put("blueprint_name", blueprintName);

            String jsonResponse = veryfiWebClient.post()
                    .header("CLIENT-ID", clientId)
                    .header("AUTHORIZATION", "apikey " + username + ":" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        int statusCode = response.statusCode().value();
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    // Handle specific error cases
                                    if (statusCode == 403) {
                                        return Mono.error(new RuntimeException(
                                            "Veryfi API quota exceeded. Please contact support to upgrade your account or try again later."
                                        ));
                                    } else if (statusCode == 401) {
                                        return Mono.error(new RuntimeException(
                                            "Veryfi API authentication failed. Please check API credentials."
                                        ));
                                    } else if (statusCode == 429) {
                                        return Mono.error(new RuntimeException(
                                            "Veryfi API rate limit exceeded. Please try again later."
                                        ));
                                    } else {
                                        return Mono.error(new RuntimeException(
                                            "Veryfi API error (" + statusCode + "): " + body
                                        ));
                                    }
                                });
                    })
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(jsonResponse);

	            res.setSuccess(true);
	            res.setReason("OK");																									
	            res.setFullname(root.path("full_name").asText("Not Found"));
	            res.setDateOfBirth(root.path("date_of_birth").asText(null));
	            res.setHeight(root.path("height").asText(null));
	            res.setDocumentNumber(root.path("id_number").asText(null));
	            res.setCreateDate(root.path("create_date").asText(null));
	            res.setExpirationDate(root.path("expiration_date").asText(null));
	            if (request.getDocType() != null) {
	                res.setDocType(request.getDocType().name().toLowerCase()); 
	            }
	            res.setGender(root.path("gender").asText(null));
	            res.setNationality(root.path("nationality").asText(null));
	            res.setAddress(root.path("address").asText(null));

	         List<String> mismatches = new ArrayList<>();

	         if (request.getFullname() != null) {
	             String reqName  = request.getFullname().trim();
	             String ocrName  = res.getFullname() != null ? res.getFullname().trim() : null;
	             if (ocrName == null || !reqName.equalsIgnoreCase(ocrName)) {
	                 mismatches.add("fullname");
	             }
	         }

	         if (request.getDob() != null) {
	             if (!Objects.equals(request.getDob(), res.getDateOfBirth())) {
	                 mismatches.add("dateOfBirth");
	             }
	         }

	         if (request.getDocType() != null) {
	             String expectedType;
	             switch (request.getDocType()) {
	                 case PASSPORT:
	                     expectedType = "passport";
	                     break;
	                 case DRIVER_LICENSE:
	                     expectedType = "driver_license";
	                     break;
	                 case ID_CARD:
	                	 expectedType = "ID_CARD";
	                	 break;
	                 default:   
	                     throw new IllegalArgumentException("Unsupported document type");
	             }

	             if (!Objects.equals(expectedType, res.getDocType())) {
	                 mismatches.add("docType");
	             }
	         }

	         if (!mismatches.isEmpty()) {
	        	 res.setSuccess(false);
	        	 res.setReason("Verification failed: data mismatch in " + String.join(", ", mismatches));
	         }
            return res;

        } catch (RuntimeException e) {
            // Handle Veryfi API specific errors
            String errorMessage = e.getMessage();
            res.setSuccess(false);
            
            if (errorMessage.contains("quota exceeded") || errorMessage.contains("scan limit")) {
                res.setReason("Service temporarily unavailable: API quota exceeded. Please try again later or contact support.");
            } else if (errorMessage.contains("authentication failed")) {
                res.setReason("Service configuration error. Please contact support.");
            } else if (errorMessage.contains("rate limit")) {
                res.setReason("Too many requests. Please wait a moment and try again.");
            } else {
                res.setReason("Verification service error: " + errorMessage);
            }
            
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            res.setSuccess(false);
            res.setReason("Document verification failed. Please ensure the image is clear and try again.");
            return res;
        }
    }
	
	private String resolveBlueprintName(IdDocType docType) {
        if (docType == null) {
            return idCardBlueprintName;
        }

        switch (docType) {
            case PASSPORT:
                return passportBlueprintName;
            case DRIVER_LICENSE:
                return driverLicenseBlueprintName;
            case ID_CARD:
            	return idCardBlueprintName;
            default:
            	throw new IllegalArgumentException("Unsupported document type: " + docType);
        }
    }

}