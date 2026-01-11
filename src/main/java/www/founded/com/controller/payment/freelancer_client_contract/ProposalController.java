package www.founded.com.controller.payment.freelancer_client_contract;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client_freelancer_contract.ProposalCreateDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;
import www.founded.com.service.payment.client_freelancer_contract.ProposalService;
@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    // Freelancer sends proposal to a job
    @PostMapping
    public ResponseEntity<Long> send(@Valid @RequestBody ProposalCreateDTO dto) {
        Long offerId = proposalService.sendProposal(dto);
        return ResponseEntity.ok(offerId);
    }

    // Client accepts proposal -> creates Project + Escrow + Milestones
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<Project> accept(
            @PathVariable Long offerId,
            @RequestParam Client clientId
    ) {
        return ResponseEntity.ok(proposalService.acceptProposal(offerId, clientId));
    }

    // Client rejects proposal
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long offerId,
            @RequestParam Client clientId,
            @RequestParam(required = false) String reason
    ) {
        proposalService.rejectProposal(offerId, clientId, reason);
        return ResponseEntity.ok().build();
    }

    // Freelancer withdraws proposal
    @PostMapping("/{offerId}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long offerId,
            @RequestParam Freelancer freelancerId
    ) {
        proposalService.withdrawProposal(offerId, freelancerId);
        return ResponseEntity.ok().build();
    }
}
