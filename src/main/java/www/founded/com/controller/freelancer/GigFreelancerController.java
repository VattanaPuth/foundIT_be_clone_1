package www.founded.com.controller.freelancer;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.freelancer.GigFreelancerClientViewDTO;
import www.founded.com.dto.freelancer.GigFreelancerDTO;
import www.founded.com.dto.pageable.PageDTO;
import www.founded.com.mapper.GigFreelancerMapper;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.service.freelancer.GigFreelancerService;

@RestController
@RequestMapping("/gigs/freelancer")
@RequiredArgsConstructor
public class GigFreelancerController {
	
	private final GigFreelancerService gigService;
	private final GigFreelancerMapper gfm;
	
	@GetMapping("{id}/freelancer") // getById
	public ResponseEntity<?> getById(@PathVariable("id") Long id){
		GigFreelancer ById = gigService.getById(id);
		return ResponseEntity.ok(ById);
	}
	
	@GetMapping // GetAllGigs | Filter & Pageable
	public ResponseEntity<?> getGigs(@RequestParam Map<String, String> params){
		Page<GigFreelancer> gigs = gigService.getGigs(params);
		PageDTO pages = new PageDTO(gigs);
		return ResponseEntity.ok(pages);
	}
	
    @GetMapping("{id}/freelancer/image") // getImage
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        GigFreelancer gig = gigService.getImage(id);
        return ResponseEntity.ok().contentType(MediaType.valueOf(gig.getImageType())).body(gig.getImageData());
    }
    
    @GetMapping("/public") // Get all public gigs for clients to view
    public ResponseEntity<?> getPublicGigs(@RequestParam Map<String, String> params) {
        Page<GigFreelancer> gigs = gigService.getPublicGigs(params);
        PageDTO pages = new PageDTO(gigs);
        return ResponseEntity.ok(pages);
    }
    
    @GetMapping("/client-view") // Get all public gigs formatted for client home page
    public ResponseEntity<?> getGigsForClientView(@RequestParam(required = false) Map<String, String> params) {
        Page<GigFreelancerClientViewDTO> gigs = gigService.getGigsForClientView(params);
        PageDTO pages = new PageDTO(gigs);
        return ResponseEntity.ok(pages);
    }
    
    @GetMapping("/{id}/client-view") // Get single gig detail for client view
    public ResponseEntity<?> getGigDetailForClientView(@PathVariable Long id) {
        GigFreelancerClientViewDTO gig = gigService.getGigDetailForClientView(id);
        return ResponseEntity.ok(gig);
    }
    
    @PostMapping("set-all-public") // Set gig as public
    public ResponseEntity<?> setPublic(@PathVariable Long id) {
        GigFreelancer updatedGig = gigService.setPublic();
        return ResponseEntity.ok(gfm.toGigFreelancerDTO(updatedGig));
    }
	
    @PostMapping(value = "{id}/upload/image", 
    			 consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // uploadImage & new upload is mean update or override the old image
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestPart("imageFile") MultipartFile imageFile) {
        gigService.uploadImage(id, imageFile);
        return ResponseEntity.ok("Image uploaded successfully");
    }
	
	@PostMapping("create") // CreateGig
	public ResponseEntity<?> createGig(@RequestBody GigFreelancerDTO gigDTO){
		GigFreelancer gf = gfm.toGigFreelancer(gigDTO);
		gf = gigService.createGig(gf);
		return ResponseEntity.ok(gfm.toGigFreelancerDTO(gf));
	}
	
	@PutMapping("{id}/update/freelancer") // UpdateGig
	public ResponseEntity<?> updateGig(@PathVariable("id") Long gigId ,@RequestBody GigFreelancerDTO gigDTO){
		GigFreelancer gig = gfm.toGigFreelancer(gigDTO);
		GigFreelancer gigUpdate = gigService.updateGig(gigId, gig);
		return ResponseEntity.ok(gfm.toGigFreelancerDTO(gigUpdate));
	}
	
	@DeleteMapping("{gigId}/delete/freelancer") // DeleteGig
	public ResponseEntity<Void> deleteGigById(@PathVariable("gigId") Long id){
		gigService.deleteGigById(id);
		return ResponseEntity.ok().build();
	}
	
    @DeleteMapping("{id}/delete/freelancer/image") // DeleteImage
    public ResponseEntity<Void> deleteImage(@PathVariable Long id){
    	gigService.deleteImage(id);
    	return ResponseEntity.ok().build();
    }
}
