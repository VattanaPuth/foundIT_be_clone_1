package www.founded.com.service.payment.client_freelancer_contract;

import www.founded.com.dto.client_freelancer_contract.ProposalCreateDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;

public interface ProposalService {
    Long sendProposal(ProposalCreateDTO dto);
    Project acceptProposal(Long offerId, Client clientId);
    void rejectProposal(Long offerId, Client clientId, String reason);
    void withdrawProposal(Long offerId, Freelancer freelancerId);
}