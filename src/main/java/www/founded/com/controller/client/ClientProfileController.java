package www.founded.com.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client.ClientProfileDTO;
import www.founded.com.service.client.impl.ClientProfileServiceImpl;

@RestController
@RequestMapping("/client/profile")
@RequiredArgsConstructor
public class ClientProfileController {

	private final ClientProfileServiceImpl profileService;

	@PostMapping("/save")
	public ResponseEntity<?> saveProfile(
			@RequestBody ClientProfileDTO profileDTO,
			Authentication authentication) {
		try {
			System.out.println("DEBUG - ClientProfileController: /save endpoint called");
			
			if (authentication == null) {
				System.out.println("DEBUG - ClientProfileController: Authentication is null!");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Authentication required");
			}
			
			String userEmail = authentication.getName();
			System.out.println("DEBUG - ClientProfileController: User email = " + userEmail);
			System.out.println("DEBUG - ClientProfileController: Profile data = " + profileDTO);
			
			ClientProfileDTO savedProfile = profileService.saveProfile(profileDTO, userEmail);
			System.out.println("DEBUG - ClientProfileController: Profile saved successfully, ID = " + savedProfile.getId());
			
			return ResponseEntity.ok(savedProfile);
		} catch (Exception e) {
			System.err.println("DEBUG - ClientProfileController: Error saving profile - " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error saving profile: " + e.getMessage());
		}
	}

	@GetMapping("/my-profile")
	public ResponseEntity<ClientProfileDTO> getMyProfile(Authentication authentication) {
		String userEmail = authentication.getName();
		ClientProfileDTO profile = profileService.getMyProfile(userEmail);
		return ResponseEntity.ok(profile);
	}

	@GetMapping("/public/{clientId}")
	public ResponseEntity<ClientProfileDTO> getPublicProfile(@PathVariable Long clientId) {
		ClientProfileDTO profile = profileService.getPublicProfile(clientId);
		return ResponseEntity.ok(profile);
	}

	@PostMapping("/publish")
	public ResponseEntity<ClientProfileDTO> publishProfile(Authentication authentication) {
		String userEmail = authentication.getName();
		ClientProfileDTO profile = profileService.publishProfile(userEmail);
		return ResponseEntity.ok(profile);
	}
}
