package www.founded.com.service.freelancer;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import www.founded.com.model.freelancer.GigFreelancer;

public interface GigFreelancerService {	
	GigFreelancer getById(Long id);
	GigFreelancer createGig(GigFreelancer createGig);
	GigFreelancer updateGig(Long id, GigFreelancer updateGig);
	GigFreelancer uploadImage(Long id, MultipartFile imageFile);
	GigFreelancer getImage(Long id);
	void deleteGigById(Long deleteGigById);
	void deleteImage(Long deleteImageId);
	Page<GigFreelancer> getGigs(Map<String, String> params);
	GigFreelancer setPublic();
}
