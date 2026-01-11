package www.founded.com.service.payment.client_freelancer_contract;

import www.founded.com.dto.client_freelancer_contract.ClientOfferCreateDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.client_freelancer_contract.ContractOffer;

public interface ContractOfferService {
    Long createClientOffer(ClientOfferCreateDTO dto);
    Project acceptOfferByFreelancer(Long offerId, Freelancer freelancerId);
    void rejectOfferByFreelancer(Long offerId,  Freelancer freelancerId, String reason);
    void cancelOfferByClient(Long offerId, Client clientId);
    ContractOffer setAllOffersPublic();
}
