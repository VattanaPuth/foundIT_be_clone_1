package www.founded.com.controller.client;


import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client.GigClientDTO;
import www.founded.com.model.client.GigClient;
import www.founded.com.service.client.impl.GigClientServiceImpl;

@RestController
@RequestMapping("/gigs/client")
@RequiredArgsConstructor
public class GigClientController {

    private final GigClientServiceImpl gigService;

    // Set all gigs to public (visible to freelancers)
    @PostMapping("/set-all-public")
    public ResponseEntity<List<GigClient>> setAllGigsPublic() {
        List<GigClient> updatedGigs = gigService.setAllPublic(); // Set all gigs to public
        return ResponseEntity.ok(updatedGigs);
    }

    // Set a single gig to public (publish)
    @PostMapping("/{id}/publish")
    public ResponseEntity<GigClient> publishGig(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        GigClient publishedGig = gigService.publishGig(id, userEmail);
        return ResponseEntity.ok(publishedGig);
    }

    // Get all public gigs (for freelancers to view)
    @GetMapping("/public")
    public ResponseEntity<Page<GigClient>> getPublicGigs(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        Page<GigClient> gigs = gigService.getPublicGigs(params, pageable); // Fetch only public gigs
        return ResponseEntity.ok(gigs);
    }

    // Create a new gig (automatically set as public)
    @PostMapping("/create")
    public ResponseEntity<GigClient> createGig(
            @RequestBody GigClientDTO gigDTO,
            Authentication authentication) {
        String userEmail = authentication.getName();
        GigClient createdGig = gigService.createGig(gigDTO, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGig);
    }

    // Get current user's gigs
    @GetMapping("/my-gigs")
    public ResponseEntity<Page<GigClient>> getMyGigs(
            Authentication authentication,
            Pageable pageable) {
        String userEmail = authentication.getName();
        Page<GigClient> myGigs = gigService.getGigsByUser(userEmail, pageable);
        return ResponseEntity.ok(myGigs);
    }

    // Update a gig
    @PutMapping("/{id}")
    public ResponseEntity<GigClient> updateGig(
            @PathVariable Long id,
            @RequestBody GigClientDTO gigDTO,
            Authentication authentication) {
        String userEmail = authentication.getName();
        GigClient updatedGig = gigService.updateGig(id, gigDTO, userEmail);
        return ResponseEntity.ok(updatedGig);
    }

    // Delete a gig
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGig(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        gigService.deleteGig(id, userEmail);
        return ResponseEntity.noContent().build();
    }
    
    // Migration helper: Update all gigs with clientId
    @PostMapping("/migrate-client-ids")
    public ResponseEntity<String> migrateClientIds() {
        try {
            int updated = gigService.migrateClientIds();
            return ResponseEntity.ok("Updated " + updated + " gigs with client IDs");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
