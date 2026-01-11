package www.founded.com.controller.client;


import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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

    // Get all public gigs (for freelancers to view)
    @GetMapping("/public")
    public ResponseEntity<Page<GigClient>> getPublicGigs(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        Page<GigClient> gigs = gigService.getPublicGigs(params, pageable); // Fetch only public gigs
        return ResponseEntity.ok(gigs);
    }
}
