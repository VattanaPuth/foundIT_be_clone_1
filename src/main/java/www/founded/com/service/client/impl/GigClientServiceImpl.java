package www.founded.com.service.client.impl;


import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client.GigClientDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.client.ClientProfile;
import www.founded.com.model.client.GigClient;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.client.ClientProfileRepository;
import www.founded.com.repository.client.ClientRepository;
import www.founded.com.repository.client.GigClientRepository;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.service.client.GigClientService;

@Service
@RequiredArgsConstructor
public class GigClientServiceImpl implements GigClientService{

    private final GigClientRepository gigRepo;
    private final UserRegisterRepository userRegisterRepository;
    private final ClientRepository clientRepository;
    private final ClientProfileRepository clientProfileRepository;

    // Set all gigs to public
    @Override
    public List<GigClient> setAllPublic() {
        List<GigClient> allGigs = gigRepo.findAll();
        for (GigClient gig : allGigs) {
            gig.setPublic(true);  // Set each gig as public
        }
        return gigRepo.saveAll(allGigs);  // Save all updated gigs
    }

    // Publish a single gig (set to public) and also publish the profile
    @Override
    public GigClient publishGig(Long gigId, String userEmail) {
        GigClient gig = gigRepo.findById(gigId)
            .orElseThrow(() -> new RuntimeException("Gig not found"));
        
        // Verify the gig belongs to the user
        if (!gig.getPostedBy().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: You can only publish your own gigs");
        }

        gig.setPublic(true);
        
        // Also publish the client's profile if it exists
        try {
            UserRegister user = userRegisterRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Client client = clientRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
            
            ClientProfile profile = clientProfileRepository.findByClient(client)
                .orElse(null);
            
            if (profile != null) {
                profile.setIsPublic(true);
                clientProfileRepository.save(profile);
            }
        } catch (Exception e) {
            // If profile doesn't exist or can't be published, just continue with gig publishing
            System.out.println("Could not publish profile: " + e.getMessage());
        }
        
        return gigRepo.save(gig);
    }

    // Get only public gigs
	@Override
	public Page<GigClient> getPublicGigs(Map<String, String> params, Pageable pageable) {
		return gigRepo.findByIsPublic(true, pageable);
	}

    // Create a new gig (automatically public)
    @Override
    public GigClient createGig(GigClientDTO gigDTO, String userEmail) {
        try {
            // Verify user exists
            UserRegister user = userRegisterRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            GigClient gig = new GigClient();
            gig.setTitle(gigDTO.getTitle());
            gig.setDescription(gigDTO.getDescription());
            gig.setCategory(gigDTO.getCategory());
            gig.setType(gigDTO.getType());
            gig.setPayMode(gigDTO.getPayMode());
            gig.setBudgetMin(gigDTO.getBudgetMin());
            gig.setBudgetMax(gigDTO.getBudgetMax());
            gig.setDeliveryTime(gigDTO.getDeliveryTime());
            gig.setImageType(gigDTO.getImageType());
            gig.setImageData(gigDTO.getImageData());
            gig.setReferenceFiles(gigDTO.getReferenceFiles());
            gig.setPublic(true); // Always set as public
            gig.setPostedBy(user.getEmail());
            
            // Try to get client ID, but don't fail if client doesn't exist
            try {
                Client client = clientRepository.findByUser_Id(user.getId())
                    .orElse(null);
                if (client != null) {
                    gig.setClientId(client.getId());
                }
            } catch (Exception e) {
                System.err.println("Could not find client for user: " + userEmail);
            }
            
            gig.setCreatedAt(new Date());

            return gigRepo.save(gig);
        } catch (Exception e) {
            System.err.println("Error creating gig: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create gig: " + e.getMessage(), e);
        }
    }

    // Get gigs by user email
    @Override
    public Page<GigClient> getGigsByUser(String userEmail, Pageable pageable) {
        return gigRepo.findByPostedBy(userEmail, pageable);
    }

    // Update a gig
    @Override
    public GigClient updateGig(Long gigId, GigClientDTO gigDTO, String userEmail) {
        GigClient gig = gigRepo.findById(gigId)
            .orElseThrow(() -> new RuntimeException("Gig not found"));
        
        // Verify the gig belongs to the user
        if (!gig.getPostedBy().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: You can only update your own gigs");
        }

        // Update fields
        gig.setTitle(gigDTO.getTitle());
        gig.setDescription(gigDTO.getDescription());
        gig.setCategory(gigDTO.getCategory());
        gig.setType(gigDTO.getType());
        gig.setPayMode(gigDTO.getPayMode());
        gig.setBudgetMin(gigDTO.getBudgetMin());
        gig.setBudgetMax(gigDTO.getBudgetMax());
        gig.setDeliveryTime(gigDTO.getDeliveryTime());
        
        if (gigDTO.getImageType() != null) {
            gig.setImageType(gigDTO.getImageType());
        }
        if (gigDTO.getImageData() != null) {
            gig.setImageData(gigDTO.getImageData());
        }
        if (gigDTO.getReferenceFiles() != null) {
            gig.setReferenceFiles(gigDTO.getReferenceFiles());
        }

        return gigRepo.save(gig);
    }

    // Delete a gig
    @Override
    public void deleteGig(Long gigId, String userEmail) {
        GigClient gig = gigRepo.findById(gigId)
            .orElseThrow(() -> new RuntimeException("Gig not found"));
        
        // Verify the gig belongs to the user
        if (!gig.getPostedBy().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: You can only delete your own gigs");
        }

        gigRepo.delete(gig);
    }

    // Migration helper: populate clientId for existing gigs
    @Override
    public int migrateClientIds() {
        List<GigClient> allGigs = gigRepo.findAll();
        int updated = 0;
        
        for (GigClient gig : allGigs) {
            if (gig.getClientId() == null && gig.getPostedBy() != null) {
                try {
                    UserRegister user = userRegisterRepository.findByEmail(gig.getPostedBy())
                        .orElse(null);
                    
                    if (user != null) {
                        Client client = clientRepository.findByUser_Id(user.getId())
                            .orElse(null);
                        
                        if (client != null) {
                            gig.setClientId(client.getId());
                            gigRepo.save(gig);
                            updated++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to migrate gig ID " + gig.getId() + ": " + e.getMessage());
                }
            }
        }
        
        return updated;
    }

    // Get a single public gig by ID
    public GigClient getPublicGigById(Long id) {
        return gigRepo.findByIdAndIsPublic(id, true).orElse(null);
    }
}
