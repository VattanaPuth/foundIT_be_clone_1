package www.founded.com.service.freelancer.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.freelancer.GigFreelancerClientViewDTO;
import www.founded.com.exception.ResourceNotFoundException;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.model.freelancer.UserSkill;
import www.founded.com.repository.freelancer.GigFreelancerRepository;
import www.founded.com.service.freelancer.GigFreelancerService;
import www.founded.com.spec.GigFreelancerSpec;
import www.founded.com.utils.freelancer.GigFreelancerSpecFilter;
import www.founded.com.utils.freelancer.PageFilter;

@Service
@RequiredArgsConstructor
public class GigFreelancerServiceImpl implements GigFreelancerService{

	public final GigFreelancerRepository gigRepository;
	
	@Override 
	public GigFreelancer getById(Long id) {
		return gigRepository.findById(id)
			.orElseThrow(()-> new ResourceNotFoundException("Gig", id));
	}

	@Override
	public GigFreelancer createGig(GigFreelancer createGig) {
		return gigRepository.save(createGig);
	}

	@Override 
	public void deleteGigById(Long deleteGigById) {
			if (!gigRepository.existsById(deleteGigById)) {
		      throw new ResourceNotFoundException("Gig ",deleteGigById);
			}
		gigRepository.deleteById(deleteGigById);
	}
	
	@Override
	public GigFreelancer getImage(Long id) {
		return gigRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Image", id));
	}
	
	@Override
	public GigFreelancer uploadImage(Long id, MultipartFile imageFile) {
		GigFreelancer imageUploads = getById(id);
		imageUploads.setImageName(imageFile.getOriginalFilename());
		imageUploads.setImageType(imageFile.getContentType());
		try {
			imageUploads.setImageData(imageFile.getBytes());
		} catch (IOException e) {
			throw new RuntimeException("Failed to read image file", e);
		}
		return gigRepository.save(imageUploads);
	}

	@Override 
	public GigFreelancer updateGig(Long id, GigFreelancer updateGig) {
		GigFreelancer gigUpdate = getById(id);
		gigUpdate.setFreelancer(updateGig.getFreelancer());
		gigUpdate.setDescription(updateGig.getDescription());
		gigUpdate.setImageData(updateGig.getImageData());
		gigUpdate.setShortBio(updateGig.getShortBio());
		gigUpdate.setUserSkill(updateGig.getUserSkill());
		gigUpdate.setPrice(updateGig.getPrice());
		return gigRepository.save(gigUpdate);
	}
	

	@Override
	public void deleteImage(Long deleteImageId) {
		GigFreelancer gigImage = gigRepository.findById(deleteImageId)
	                .orElseThrow(() -> new ResourceNotFoundException("Image", deleteImageId));

		gigImage.setImageData(null);
		gigImage.setImageType(null);
		gigImage.setImageName(null);
		gigRepository.save(gigImage);
	}

	@Override
	public Page<GigFreelancer> getGigs(Map<String, String> params) {
		
		// Spec
		GigFreelancerSpecFilter gigFilter = new GigFreelancerSpecFilter();
		
		if(params.containsKey("id")) {
			String id = params.get("id");
			gigFilter.setId(Long.parseLong(id));
		}
		
		if(params.containsKey("name")) {
			String name = params.get("name");
			gigFilter.setName(name);
		}
		
		if(params.containsKey("description")) {
			String des = params.get("description");
			gigFilter.setDescription(des);;
		}
		
		if(params.containsKey("price")) {
			String priceStr = params.get("price");
			try {
				BigDecimal price = new BigDecimal(priceStr);
				gigFilter.setPrice(price);
			}catch(NumberFormatException e){
				System.out.print("Invalid Number");
			}
		}
		
		//------- Pageable -------//
		int pageNumber = PageFilter.DEFAULT_PAGE_NUMBER;
		if(params.containsKey(PageFilter.PAGE_NUMBER)) {
			pageNumber = Integer.parseInt(params.get(PageFilter.PAGE_NUMBER));
		}
		
		int pageSize = PageFilter.DEFAULT_PAGE_LIMIT;
		if(params.containsKey(PageFilter.PAGE_LIMIT)){
			pageSize = Integer.parseInt(params.get(PageFilter.PAGE_LIMIT));
		}
		
		// Spec
		GigFreelancerSpec spec = new GigFreelancerSpec(gigFilter);
		
		// Pageable
		Pageable page = PageFilter.getPageable(pageNumber, pageSize); 
		return gigRepository.findAll(spec, page);
	}

    // Fetch public gigs (Client-facing)
    @Override
    public Page<GigFreelancer> getPublicGigs(Map<String, String> params) {
        int pageNumber = PageFilter.DEFAULT_PAGE_NUMBER;
        if(params != null && params.containsKey(PageFilter.PAGE_NUMBER)) {
            pageNumber = Integer.parseInt(params.get(PageFilter.PAGE_NUMBER));
        }
        
        int pageSize = PageFilter.DEFAULT_PAGE_LIMIT;
        if(params != null && params.containsKey(PageFilter.PAGE_LIMIT)){
            pageSize = Integer.parseInt(params.get(PageFilter.PAGE_LIMIT));
        }
        
        Pageable page = PageFilter.getPageable(pageNumber, pageSize);
        return gigRepository.findByIsPublic(true, page);
    }
    
    // Get gigs formatted for client home view
    @Override
    public Page<GigFreelancerClientViewDTO> getGigsForClientView(Map<String, String> params) {
        int pageNumber = PageFilter.DEFAULT_PAGE_NUMBER;
        if(params != null && params.containsKey(PageFilter.PAGE_NUMBER)) {
            pageNumber = Integer.parseInt(params.get(PageFilter.PAGE_NUMBER));
        }
        
        int pageSize = 20; // Default to 20 for client view
        if(params != null && params.containsKey(PageFilter.PAGE_LIMIT)){
            pageSize = Integer.parseInt(params.get(PageFilter.PAGE_LIMIT));
        }
        
        Pageable page = PageFilter.getPageable(pageNumber, pageSize);
        Page<GigFreelancer> gigs = gigRepository.findByIsPublic(true, page);
        
        // Map to client view DTO
        return gigs.map(gig -> {
            GigFreelancerClientViewDTO dto = new GigFreelancerClientViewDTO();
            dto.setId(gig.getId());
            dto.setShortBio(gig.getShortBio());
            dto.setDescription(gig.getDescription());
            dto.setPrice(gig.getPrice());
            dto.setImageType(gig.getImageType());
            dto.setImageData(gig.getImageData());
            dto.setVerified(false); // Default value
            
            // Get freelancer information if available
            Freelancer freelancer = gig.getFreelancer();
            if (freelancer != null) {
                dto.setFreelancerName(freelancer.getName() != null ? freelancer.getName() : "Freelancer");
                // Set default values since Freelancer model is minimal
                dto.setExperience("Intermediate");
                dto.setLocation("Cambodia");
            } else {
                dto.setFreelancerName("Unknown");
                dto.setExperience("Entry");
                dto.setLocation("Unknown");
            }
            
            // Get skill information if available
            UserSkill skill = gig.getUserSkill();
            if (skill != null) {
                dto.setSkillName(skill.getSkill());
            } else {
                dto.setSkillName("General");
            }
            
            // Set default/calculated values for missing fields
            dto.setRating(4.5 + (Math.random() * 0.5)); // Random rating between 4.5-5.0
            dto.setReviewCount((int) (Math.random() * 200) + 50); // Random reviews 50-250
            dto.setLastActiveDays((int) (Math.random() * 15)); // Random 0-15 days
            dto.setWorkCount((int) (Math.random() * 150) + 50); // Random work count 50-200
            
            return dto;
        });
    }

	@Override
	public GigFreelancer setPublic() {
		List<GigFreelancer> allGigs = gigRepository.findAll();
        for (GigFreelancer gig : allGigs) {
            gig.setPublic(true);  // Set each gig as public
        }
        return  (GigFreelancer) gigRepository.saveAll(allGigs);  // Save all updated gigs
    }
}
