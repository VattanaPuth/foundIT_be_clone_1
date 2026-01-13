package www.founded.com.repository.client_freelancer_contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import www.founded.com.enum_.client_freelancer_contract.OfferDirection;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.client_freelancer_contract.ContractOffer;

public interface ContractOfferRepository extends JpaRepository<ContractOffer, Long> {
    List<ContractOffer> findByClientId(Client clientId);
    List<ContractOffer> findByFreelancerId(Freelancer freelancerId);
    List<ContractOffer> findByClientId_IdAndDirection(Long clientId, OfferDirection direction);
    List<ContractOffer> findByFreelancerId_IdAndDirection(Long freelancerId, OfferDirection direction);
    Optional<ContractOffer> findByIdAndClientId(Long id, Client clientId);
    Optional<ContractOffer> findByIdAndFreelancerId(Long id, Freelancer freelancerId);
    List<ContractOffer> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}
