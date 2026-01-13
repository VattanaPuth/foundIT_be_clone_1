package www.founded.com.service.payment.client_freelancer_contract;

import java.util.List;

import www.founded.com.dto.client_freelancer_contract.ProposalCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ProposalDetailDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.freelancer_client_contract.Proposal;

public interface ProposalService {
    Proposal getProposalById(Long proposalId);
    ProposalDetailDTO getProposalDetailById(Long proposalId);
    List<Proposal> getProposalsForClient(Long clientId);
    List<ProposalDetailDTO> getProposalDetailsForClient(Long clientId);
    List<Proposal> getProposalsForFreelancer(Long freelancerId);
    Long sendProposal(ProposalCreateDTO dto);
    Project acceptProposal(Long offerId, Client clientId);
    void rejectProposal(Long offerId, Client clientId, String reason);
    void withdrawProposal(Long offerId, Freelancer freelancerId);
}