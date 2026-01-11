package www.founded.com.controller.payment.client_freelancer_contract;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client_freelancer_contract.ClientOfferCreateDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;
import www.founded.com.service.payment.client_freelancer_contract.ContractOfferService;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
public class ContractOfferController {

    private final ContractOfferService offerService;
    
    // Client -> Freelancer
    @PostMapping("/client-to-freelancer")
    public ResponseEntity<Long> createClientOffer(@Valid @RequestBody ClientOfferCreateDTO dto) {
        return ResponseEntity.ok(offerService.createClientOffer(dto));
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
}