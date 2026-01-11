package www.founded.com.repository.client_freelancer_contract;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import www.founded.com.model.payment.client_freelancer_contract.ContractOfferMilestone;

public interface ContractOfferMilestoneRepository extends JpaRepository<ContractOfferMilestone, Long> {
    List<ContractOfferMilestone> findByOfferIdOrderByOrderIndexAsc(Long offerId);
}
