package www.founded.com.service.client.impl;


import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client.GigClientDTO;
import www.founded.com.model.client.GigClient;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.client.GigClientRepository;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.service.client.GigClientService;

@Service
@RequiredArgsConstructor
public class GigClientServiceImpl implements GigClientService{

    private final GigClientRepository gigRepo;
    private final UserRegisterRepository userRegisterRepository;

    // Set all gigs to public
    public List<GigClient> setAllPublic() {
        List<GigClient> allGigs = gigRepo.findAll();
        for (GigClient gig : allGigs) {
            gig.setPublic(true);  // Set each gig as public
        }
        return gigRepo.saveAll(allGigs);  // Save all updated gigs
    }

    // Get only public gigs
	@Override
	public Page<GigClient> getPublicGigs(Map<String, String> params, Pageable pageable) {
		return gigRepo.findByIsPublic(true, pageable);
	}

    // Create a new gig (automatically public)
    public GigClient createGig(GigClientDTO gigDTO, String userEmail) {
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
        gig.setCreatedAt(new Date());

        return gigRepo.save(gig);
    }

    // Get gigs by user email
    public Page<GigClient> getGigsByUser(String userEmail, Pageable pageable) {
        return gigRepo.findByPostedBy(userEmail, pageable);
    }

    // Update a gig
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
    public void deleteGig(Long gigId, String userEmail) {
        GigClient gig = gigRepo.findById(gigId)
            .orElseThrow(() -> new RuntimeException("Gig not found"));
        
        // Verify the gig belongs to the user
        if (!gig.getPostedBy().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: You can only delete your own gigs");
        }

        gigRepo.delete(gig);
    }

}
