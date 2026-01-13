package www.founded.com.controller.payment.client_freelancer_contract;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import www.founded.com.dto.client_freelancer_contract.ClientOfferCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ContractOfferViewDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;
import www.founded.com.service.payment.client_freelancer_contract.ContractOfferService;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@Slf4j
public class ContractOfferController {

    private final ContractOfferService offerService;
    
    // Client -> Freelancer
    @PostMapping("/client-to-freelancer")
    public ResponseEntity<?> createClientOffer(@Valid @RequestBody ClientOfferCreateDTO dto) {
        try {
            log.info("Creating offer - clientId: {}, freelancerId: {}, gigId: {}", 
                dto.getClientId(), dto.getFreelancerId(), dto.getGigId());
            Long offerId = offerService.createClientOffer(dto);
            log.info("Offer created successfully with ID: {}", offerId);
            return ResponseEntity.ok(offerId);
        } catch (Exception e) {
            log.error("Error creating offer: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Freelancer accepts -> creates Project + Escrow + Milestones
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<Project> accept(@PathVariable Long offerId, @RequestParam Freelancer freelancerId) {
        return ResponseEntity.ok(offerService.acceptOfferByFreelancer(offerId, freelancerId));
    }

    // Freelancer rejects
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long offerId,
            @RequestParam Freelancer freelancerId,
            @RequestParam(required = false) String reason
    ) {
        offerService.rejectOfferByFreelancer(offerId, freelancerId, reason);
        return ResponseEntity.ok().build();
    }

    // Client cancels before acceptance
    @PostMapping("/{offerId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long offerId, @RequestParam Client clientId) {
        offerService.cancelOfferByClient(offerId, clientId);
        return ResponseEntity.ok().build();
    }
    
    // Get all offers sent by a client (by user ID)
    @GetMapping("/client/{userId}")
    public ResponseEntity<List<ContractOfferViewDTO>> getClientOffers(@PathVariable Long userId) {
        return ResponseEntity.ok(offerService.getOffersByClientUserId(userId));
    }
    
    // Get all offers received by a freelancer
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<ContractOfferViewDTO>> getFreelancerOffers(@PathVariable Long freelancerId) {
        return ResponseEntity.ok(offerService.getOffersByFreelancerId(freelancerId));
    }
    
    // Get single offer details
    @GetMapping("/{offerId}")
    public ResponseEntity<ContractOfferViewDTO> getOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(offerService.getOfferById(offerId));
    }
}