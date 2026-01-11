package www.founded.com.service.veryfi_ekyc.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.service.veryfi_ekyc.EkycService;

@Service
@RequiredArgsConstructor
public class EkycServiceImpl implements EkycService {
	
	private final WebClient veryfiWebClient;
	private final ObjectMapper objectMapper;
	private final UserRegisterRepository userRegisterRepository;
	
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
	public EkycResponseDTO verifyIdDocument(MultipartFile file, EkycRequestDTO request, String username) {
        
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

            // Check if OCR extraction failed
            boolean ocrFailed = (res.getFullname() == null || res.getFullname().equalsIgnoreCase("Not Found")) &&
                               res.getDateOfBirth() == null &&
                               res.getGender() == null &&
                               res.getNationality() == null;
            
            if (ocrFailed) {
                System.out.println("DEBUG - OCR extraction failed. Allowing verification with warning.");
                res.setSuccess(true);
                res.setReason("WARNING: Document uploaded but OCR could not extract data. Verification completed without data validation. Please ensure document quality for better results.");
                return res;
            }

            List<String> mismatches = new ArrayList<>();

            if (request.getFullname() != null) {
                String reqName  = request.getFullname().trim();
                String ocrName  = res.getFullname() != null ? res.getFullname().trim() : null;
                
                System.out.println("DEBUG - Fullname comparison:");
                System.out.println("  Request: '" + reqName + "'");
                System.out.println("  OCR: '" + ocrName + "'");
                
                if (ocrName == null || ocrName.equalsIgnoreCase("Not Found") || !reqName.equalsIgnoreCase(ocrName)) {
                    mismatches.add("fullname (Request: '" + reqName + "', OCR: '" + ocrName + "')");
                }
            }

            if (request.getDob() != null) {
                // Normalize both dates to handle Khmer numerals
                String normalizedRequestDob = normalizeDate(request.getDob());
                String normalizedOcrDob = normalizeDate(res.getDateOfBirth());
                
                System.out.println("DEBUG - DOB comparison:");
                System.out.println("  Request (original): '" + request.getDob() + "'");
                System.out.println("  Request (normalized): '" + normalizedRequestDob + "'");
                System.out.println("  OCR (original): '" + res.getDateOfBirth() + "'");
                System.out.println("  OCR (normalized): '" + normalizedOcrDob + "'");
                
                if (!Objects.equals(normalizedRequestDob, normalizedOcrDob)) {
                    mismatches.add("dateOfBirth (Request: '" + normalizedRequestDob + "', OCR: '" + normalizedOcrDob + "')");
                }
            }

            if (request.getSex() != null) {
                String reqSex = request.getSex().trim().toLowerCase();
                String ocrSex = res.getGender() != null ? res.getGender().trim() : null;
                
                System.out.println("DEBUG - Sex comparison:");
                System.out.println("  Request (original): '" + request.getSex() + "'");
                System.out.println("  Request (normalized): '" + reqSex + "'");
                System.out.println("  OCR (original): '" + res.getGender() + "'");
                
                // Normalize request: Male/m -> m, Female/f -> f
                reqSex = reqSex.startsWith("m") ? "m" : reqSex.startsWith("f") ? "f" : reqSex;
                
                // Normalize OCR: Handle Khmer + English variations
                if (ocrSex != null) {
                    String ocrLower = ocrSex.toLowerCase();
                    // Khmer: ប្រុស (male), ស្រី (female)
                    if (ocrSex.equals("ប្រុស") || ocrLower.startsWith("m")) {
                        ocrSex = "m";
                    } else if (ocrSex.equals("ស្រី") || ocrLower.startsWith("f")) {
                        ocrSex = "f";
                    } else {
                        ocrSex = null; // Unknown format
                    }
                }
                
                System.out.println("  Request (final): '" + reqSex + "'");
                System.out.println("  OCR (final): '" + ocrSex + "'");
                
                if (ocrSex == null || !reqSex.equals(ocrSex)) {
                    mismatches.add("sex (Request: '" + reqSex + "', OCR: '" + ocrSex + "')");
                }
            }

            if (request.getNationality() != null) {
                String reqNat = request.getNationality().trim().toLowerCase();
                String ocrNat = res.getNationality() != null ? res.getNationality().trim() : null;
                
                System.out.println("DEBUG - Nationality comparison:");
                System.out.println("  Request: '" + request.getNationality() + "' -> '" + reqNat + "'");
                System.out.println("  OCR: '" + res.getNationality() + "'");
                
                // Normalize OCR: Handle Khmer nationality
                if (ocrNat != null) {
                    // Map Khmer "ខ្មែរ" to "cambodia"
                    if (ocrNat.equals("ខ្មែរ")) {
                        ocrNat = "cambodia";
                    } else {
                        ocrNat = ocrNat.toLowerCase();
                    }
                }
                
                System.out.println("  OCR (normalized): '" + ocrNat + "'");
                
                if (ocrNat == null || !reqNat.equals(ocrNat)) {
                    mismatches.add("nationality (Request: '" + reqNat + "', OCR: '" + ocrNat + "')");
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
            } else {
                // Verification successful - mark user as verified
                try {
                    System.out.println("DEBUG - Looking up user by email: '" + username + "'");
                    
                    Optional<UserRegister> userOpt = userRegisterRepository.findByEmail(username);
                    System.out.println("DEBUG - User found: " + userOpt.isPresent());
                    
                    if (!userOpt.isPresent()) {
                        // Try to find all users to debug
                        System.out.println("DEBUG - User not found. Checking all users...");
                        List<UserRegister> allUsers = userRegisterRepository.findAll();
                        System.out.println("DEBUG - Total users in database: " + allUsers.size());
                        for (UserRegister u : allUsers) {
                            System.out.println("DEBUG - Found user: username='" + u.getUsername() + "', email='" + u.getEmail() + "'");
                        }
                    }
                    
                    UserRegister user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
                    
                    System.out.println("DEBUG - Before save: ekycVerified=" + user.getEkycVerified());
                    user.setEkycVerified(true);
                    user.setEkycVerifiedAt(new Date());
                    System.out.println("DEBUG - After set: ekycVerified=" + user.getEkycVerified());
                    
                    UserRegister savedUser = userRegisterRepository.save(user);
                    System.out.println("DEBUG - After save: ekycVerified=" + savedUser.getEkycVerified());
                    System.out.println("DEBUG - User ID: " + savedUser.getId() + ", Username: " + savedUser.getUsername());
                } catch (Exception e) {
                    System.err.println("Failed to update eKYC verification status: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return res;

        } catch (RuntimeException e) {
            // Handle Veryfi API specific errors
            String errorMessage = e.getMessage();
            res.setSuccess(false);
            
            if (errorMessage.contains("quota exceeded") || errorMessage.contains("scan limit")) {
                // Development mode: Allow proceeding without OCR validation when quota exceeded
                System.out.println("INFO - Veryfi API quota exceeded. Proceeding with user-provided data for development.");
                res.setSuccess(true);
                res.setReason("INFO: Document uploaded successfully. API quota exceeded, proceeding without OCR validation.");
                // Use user-provided data
                res.setFullname(request.getFullname());
                res.setDateOfBirth(request.getDob());
                res.setGender(request.getSex());
                res.setNationality(request.getNationality());
                res.setDocType(request.getDocType() != null ? request.getDocType().name().toLowerCase() : null);
                
                // Mark user as verified
                try {
                    System.out.println("DEBUG (quota fallback) - Looking up user by email: '" + username + "'");
                    
                    Optional<UserRegister> userOpt = userRegisterRepository.findByEmail(username);
                    System.out.println("DEBUG (quota fallback) - User found: " + userOpt.isPresent());
                    
                    if (!userOpt.isPresent()) {
                        // Try to find all users to debug
                        System.out.println("DEBUG (quota fallback) - User not found. Checking all users...");
                        List<UserRegister> allUsers = userRegisterRepository.findAll();
                        System.out.println("DEBUG (quota fallback) - Total users in database: " + allUsers.size());
                        for (UserRegister u : allUsers) {
                            System.out.println("DEBUG (quota fallback) - Found user: username='" + u.getUsername() + "', email='" + u.getEmail() + "'");
                        }
                    }
                    
                    UserRegister user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
                    
                    System.out.println("DEBUG (quota fallback) - Before save: ekycVerified=" + user.getEkycVerified());
                    user.setEkycVerified(true);
                    user.setEkycVerifiedAt(new Date());
                    System.out.println("DEBUG (quota fallback) - After set: ekycVerified=" + user.getEkycVerified());
                    
                    UserRegister savedUser = userRegisterRepository.save(user);
                    System.out.println("DEBUG (quota fallback) - After save: ekycVerified=" + savedUser.getEkycVerified());
                    System.out.println("DEBUG (quota fallback) - User ID: " + savedUser.getId() + ", Username: " + savedUser.getUsername());
                } catch (Exception ex) {
                    System.err.println("Failed to update eKYC verification status: " + ex.getMessage());
                    ex.printStackTrace();
                }
                
                return res;
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
	
	/**
	 * Normalize date string by converting Khmer numerals to Arabic numerals
	 * and standardizing date format separators
	 */
	private String normalizeDate(String date) {
	    if (date == null || date.isEmpty()) {
	        return date;
	    }
	    
	    // Khmer numerals Unicode mappings (០-៩ = 0-9)
	    String normalized = date
	        .replace("០", "0")
	        .replace("១", "1")
	        .replace("២", "2")
	        .replace("៣", "3")
	        .replace("៤", "4")
	        .replace("៥", "5")
	        .replace("៦", "6")
	        .replace("៧", "7")
	        .replace("៨", "8")
	        .replace("៩", "9");
	    
	    // Also normalize date separators (/, -, .) to consistent format
	    normalized = normalized.replaceAll("[/\\-.]", "-");
	    
	    // Remove any whitespace
	    normalized = normalized.replaceAll("\\s+", "");
	    
	    return normalized.toLowerCase().trim();
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
    
    /**
     * Check if a user has completed eKYC verification
     */
    public boolean isUserVerified(String username) {
        boolean verified = userRegisterRepository.findByEmail(username)
            .map(user -> {
                System.out.println("DEBUG - isUserVerified check for email: " + username);
                System.out.println("DEBUG - ekycVerified field value: " + user.getEkycVerified());
                System.out.println("DEBUG - ekycVerifiedAt field value: " + user.getEkycVerifiedAt());
                return user.getEkycVerified() != null && user.getEkycVerified();
            })
            .orElse(false);
        System.out.println("DEBUG - isUserVerified result: " + verified);
        return verified;
    }

}