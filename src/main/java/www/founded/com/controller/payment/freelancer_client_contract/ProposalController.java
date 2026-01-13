package www.founded.com.controller.payment.freelancer_client_contract;

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
import www.founded.com.dto.client_freelancer_contract.ProposalCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ProposalDetailDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.freelancer_client_contract.Proposal;
import www.founded.com.service.payment.client_freelancer_contract.ProposalService;
@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    // Get proposal by ID with full details
    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalDetailDTO> getProposalById(@PathVariable Long proposalId) {
        return ResponseEntity.ok(proposalService.getProposalDetailById(proposalId));
    }

    // Get all proposals for a client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProposalDetailDTO>> getProposalsForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(proposalService.getProposalDetailsForClient(clientId));
    }

    // Get all proposals for a freelancer
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<Proposal>> getProposalsForFreelancer(@PathVariable Long freelancerId) {
        return ResponseEntity.ok(proposalService.getProposalsForFreelancer(freelancerId));
    }

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
