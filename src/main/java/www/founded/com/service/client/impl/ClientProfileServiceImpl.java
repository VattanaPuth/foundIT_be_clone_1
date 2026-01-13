package www.founded.com.service.client.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client.ClientProfileDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.client.ClientProfile;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.client.ClientProfileRepository;
import www.founded.com.repository.client.ClientRepository;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.service.client.ClientProfileService;

@Service
@RequiredArgsConstructor
public class ClientProfileServiceImpl implements ClientProfileService {

	private final ClientProfileRepository profileRepository;
	private final ClientRepository clientRepository;
	private final UserRegisterRepository userRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@Transactional
	public ClientProfileDTO saveProfile(ClientProfileDTO profileDTO, String userEmail) {
		if (profileDTO == null) {
			throw new RuntimeException("Profile data is required");
		}

		UserRegister user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Client client = clientRepository.findByUser_Id(user.getId())
				.orElseGet(() -> {
					Client created = new Client();
					created.setUser(user);
					String name = profileDTO.getFullName();
					if (name == null || name.trim().isEmpty()) {
						name = user.getEmail();
					}
					created.setName(name);
					return clientRepository.save(created);
				});

		ClientProfile profile = profileRepository.findByClient(client)
				.orElse(new ClientProfile());

		profile.setClient(client);
		profile.setAvatarUrl(profileDTO.getAvatarUrl());
		profile.setFullName(profileDTO.getFullName());
		profile.setTitleRole(profileDTO.getTitleRole());
		profile.setLocation(profileDTO.getLocation());
		profile.setAllowMessages(profileDTO.getAllowMessages());
		profile.setShortBio(profileDTO.getShortBio());
		
		// Convert lists to JSON strings
		profile.setValuesWhenHiring(toJson(profileDTO.getValuesWhenHiring()));
		profile.setIndustries(toJson(profileDTO.getIndustries()));
		profile.setPreferredWorkStyles(toJson(profileDTO.getPreferredWorkStyles()));
		profile.setHireCategories(toJson(profileDTO.getHireCategories()));
		
		profile.setFixedProjectMedian(profileDTO.getFixedProjectMedian());
		profile.setHourlyMedian(profileDTO.getHourlyMedian());
		profile.setContractLengthMedian(profileDTO.getContractLengthMedian());
		profile.setWebsite(profileDTO.getWebsite());
		profile.setLinkedin(profileDTO.getLinkedin());
		profile.setXTwitter(profileDTO.getXTwitter());

		ClientProfile savedProfile = profileRepository.save(profile);
		return toDTO(savedProfile);
	}

	@Override
	@Transactional(readOnly = true)
	public ClientProfileDTO getMyProfile(String userEmail) {
		UserRegister user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Client client = clientRepository.findByUser_Id(user.getId())
				.orElseGet(() -> {
					Client created = new Client();
					created.setUser(user);
					created.setName(user.getEmail());
					return clientRepository.save(created);
				});

		ClientProfile profile = profileRepository.findByClient(client)
				.orElse(null);

		return profile != null ? toDTO(profile) : new ClientProfileDTO();
	}

	@Override
	@Transactional(readOnly = true)
	public ClientProfileDTO getPublicProfile(Long clientId) {
		try {
			ClientProfile profile = profileRepository.findByClient_IdAndIsPublicTrue(clientId)
					.orElseThrow(() -> new RuntimeException("Public profile not found for client ID: " + clientId + ". Profile may not exist or is not published yet."));

			return toDTO(profile);
		} catch (Exception e) {
			System.err.println("Error fetching public profile for client ID " + clientId + ": " + e.getMessage());
			throw new RuntimeException("Failed to fetch public profile: " + e.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public ClientProfileDTO publishProfile(String userEmail) {
		UserRegister user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Client client = clientRepository.findByUser_Id(user.getId())
				.orElseThrow(() -> new RuntimeException("Client not found"));

		ClientProfile profile = profileRepository.findByClient(client)
				.orElseThrow(() -> new RuntimeException("Profile not found. Please save your profile first."));

		profile.setIsPublic(true);
		ClientProfile publishedProfile = profileRepository.save(profile);
		
		return toDTO(publishedProfile);
	}

	private ClientProfileDTO toDTO(ClientProfile profile) {
		ClientProfileDTO dto = new ClientProfileDTO();
		dto.setId(profile.getId());
		dto.setAvatarUrl(profile.getAvatarUrl());
		dto.setFullName(profile.getFullName());
		dto.setTitleRole(profile.getTitleRole());
		dto.setLocation(profile.getLocation());
		dto.setAllowMessages(profile.getAllowMessages());
		dto.setShortBio(profile.getShortBio());
		
		dto.setValuesWhenHiring(fromJson(profile.getValuesWhenHiring()));
		dto.setIndustries(fromJson(profile.getIndustries()));
		dto.setPreferredWorkStyles(fromJson(profile.getPreferredWorkStyles()));
		dto.setHireCategories(fromJson(profile.getHireCategories()));
		
		dto.setFixedProjectMedian(profile.getFixedProjectMedian());
		dto.setHourlyMedian(profile.getHourlyMedian());
		dto.setContractLengthMedian(profile.getContractLengthMedian());
		dto.setWebsite(profile.getWebsite());
		dto.setLinkedin(profile.getLinkedin());
		dto.setXTwitter(profile.getXTwitter());
		dto.setIsPublic(profile.getIsPublic());
		
		return dto;
	}

	private String toJson(List<String> list) {
		if (list == null || list.isEmpty()) {
			return "[]";
		}
		try {
			return objectMapper.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			return "[]";
		}
	}

	private List<String> fromJson(String json) {
		if (json == null || json.isEmpty()) {
			return Collections.emptyList();
		}
		try {
			return objectMapper.readValue(json, 
				objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
		} catch (JsonProcessingException e) {
			return Collections.emptyList();
		}
	}
}
