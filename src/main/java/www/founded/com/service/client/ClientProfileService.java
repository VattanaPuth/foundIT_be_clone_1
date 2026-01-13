package www.founded.com.service.client;

import www.founded.com.dto.client.ClientProfileDTO;

public interface ClientProfileService {
	ClientProfileDTO saveProfile(ClientProfileDTO profileDTO, String userEmail);
	ClientProfileDTO getMyProfile(String userEmail);
	ClientProfileDTO getPublicProfile(Long clientId);
	ClientProfileDTO publishProfile(String userEmail);
}
