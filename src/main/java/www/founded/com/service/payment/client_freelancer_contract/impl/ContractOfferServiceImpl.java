
package www.founded.com.service.payment.client_freelancer_contract.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client_freelancer_contract.ClientOfferCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ClientOfferMilestoneCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ContractOfferMilestoneViewDTO;
import www.founded.com.dto.client_freelancer_contract.ContractOfferViewDTO;
import www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus;
import www.founded.com.enum_.client_freelancer_contract.OfferDirection;
import www.founded.com.enum_.client_freelancer_contract.OfferSourceType;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.payment.MilestoneStatus;
import www.founded.com.enum_.payment.ProjectStatus;
import www.founded.com.mapper.ContractOfferMapper;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Milestone;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.client_freelancer_contract.ContractOffer;
import www.founded.com.model.payment.client_freelancer_contract.ContractOfferMilestone;
import www.founded.com.repository.client_freelancer_contract.ContractOfferMilestoneRepository;
import www.founded.com.repository.client_freelancer_contract.ContractOfferRepository;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.payment.MilestoneRepository;
import www.founded.com.repository.payment.ProjectRepository;
import www.founded.com.service.client.ClientService;
import www.founded.com.service.freelancer.FreelancerService;
import www.founded.com.service.payment.client_freelancer_contract.ContractOfferService;

@Service
@RequiredArgsConstructor
public class ContractOfferServiceImpl implements ContractOfferService {

    private final ContractOfferRepository offerRepo;
    private final ContractOfferMilestoneRepository offerMsRepo;

    private final ProjectRepository projectRepo;
    private final EscrowRepository escrowRepo;
    private final MilestoneRepository milestoneRepo;

    private final ClientService clientService;
    private final FreelancerService freelancerService;
    private final ContractOfferMapper com;

    // -----------------------------
    // Client sends offer to freelancer (REPLACES HireRequest)
    // -----------------------------
    @Override
    @Transactional
    public Long createClientOffer(ClientOfferCreateDTO dto) {
    	
    	// Validate and fetch client by user ID, freelancer by ID
        Client client = clientService.getByUserId(dto.getClientId());
        Freelancer freelancer = freelancerService.getById(dto.getFreelancerId());
        
    	// Validate milestone sum equals totalBudget
        validateMilestoneSum(dto);

        ContractOffer offer = new ContractOffer();
        offer.setClientId(client);
        offer.setFreelancerId(freelancer);
        offer.setDirection(OfferDirection.CLIENT_TO_FREELANCER);
        offer.setSourceType(dto.getGigId() != null ? OfferSourceType.GIG : OfferSourceType.DIRECT);
        offer.setSourceId(dto.getGigId());

        offer.setTitle(dto.getTitle());
        offer.setDescription(dto.getDescription());
        offer.setTotalBudget(dto.getTotalBudget());
        offer.setCurrency(dto.getCurrency() == null ? "USD" : dto.getCurrency());
        offer.setMessage(dto.getMessage());
        offer.setExpiresAt(dto.getExpiresAt());
        offer.setIsPublic(false); // Default to not public

        offer.setStatus(ContractOfferStatus.SENT);
        offer = offerRepo.save(offer);

        for (int i = 0; i < dto.getMilestones().size(); i++) {
            ClientOfferMilestoneCreateDTO m = dto.getMilestones().get(i);
            ContractOfferMilestone ms = new ContractOfferMilestone();
            ms.setOfferId(offer.getId());
            ms.setDescription(m.getDescription());
            ms.setAmount(m.getAmount());
            ms.setOrderIndex(i + 1);
            offerMsRepo.save(ms);
        }

        return offer.getId();
    }
    
    // milestone sum == totalBudget
    private void validateMilestoneSum(ClientOfferCreateDTO dto) {
        BigDecimal sum = dto.getMilestones().stream()
                .map(ClientOfferMilestoneCreateDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(dto.getTotalBudget()) != 0) {
            throw new IllegalArgumentException("Milestones total must equal totalBudget.");
        }
    }

    // -----------------------------
    // Freelancer accepts offer -> create Project + Escrow + Milestones
    // -----------------------------
    @Override
    @Transactional
    public Project acceptOfferByFreelancer(Long offerId, Freelancer freelancerId) {

        ContractOffer offer = offerRepo.findByIdAndFreelancerId(offerId, freelancerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not yours"));

        if (offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(Instant.now())) {
            offer.setStatus(ContractOfferStatus.EXPIRED);
            offerRepo.save(offer);
            throw new IllegalStateException("Offer expired.");
        }

        // idempotent accept
        if (offer.getStatus() == ContractOfferStatus.ACCEPTED && offer.getProjectId() != null) {
            return projectRepo.findById(offer.getProjectId())
                    .orElseThrow(() -> new IllegalStateException("Project missing for accepted offer"));
        }

        if (offer.getStatus() != ContractOfferStatus.SENT) {
            throw new IllegalStateException("Offer cannot be accepted in status: " + offer.getStatus());
        }

        Long client_Id = offer.getClientId().getId();
        Long freelancer_Id = offer.getFreelancerId().getId();
        
        Client client = clientService.getById(client_Id);
        Freelancer freelancer = freelancerService.getById(freelancer_Id);

        // create escrow
        Escrow escrow = new Escrow();
        escrow.setClient(client);
        escrow.setFreelancer(freelancer);
        escrow.setTotalAmount(offer.getTotalBudget());
        escrow.setAmountPaidToFreelancer(BigDecimal.ZERO);
        escrow.setAmountRefundedToClient(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.CREATED);
        escrow = escrowRepo.save(escrow);

        // create milestones from offer milestones
        var msList = offerMsRepo.findByOfferIdOrderByOrderIndexAsc(offer.getId());
        for (ContractOfferMilestone m : msList) {
            Milestone ms = new Milestone();
            ms.setEscrow(escrow);
            ms.setDescription(m.getDescription());
            ms.setAmount(m.getAmount());
            ms.setStatus(MilestoneStatus.NOT_STARTED);
            milestoneRepo.save(ms);
        }

        // create project
        Project project = new Project();
        project.setClient(client);
        project.setFreelancer(freelancer);
        project.setEscrow(escrow);
        project.setStatus(ProjectStatus.CREATED);
        project.setTitle(offer.getTitle());
        project.setDescription(offer.getDescription());
        project.setTotalBudget(offer.getTotalBudget());

        project.setContractOfferId(offer.getId()); // ✅ unified link
        project = projectRepo.save(project);

        // update offer
        offer.setStatus(ContractOfferStatus.ACCEPTED);
        offer.setAcceptedAt(Instant.now());
        offer.setProjectId(project.getId());
        offerRepo.save(offer);

        return project;
    }

    @Override
    @Transactional
    public void rejectOfferByFreelancer(Long offerId, Freelancer freelancerId, String reason) {
        ContractOffer offer = offerRepo.findByIdAndFreelancerId(offerId, freelancerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not yours"));

        if (offer.getStatus() != ContractOfferStatus.SENT) {
            throw new IllegalStateException("Offer cannot be rejected in status: " + offer.getStatus());
        }

        offer.setStatus(ContractOfferStatus.REJECTED);
        offer.setRejectedAt(Instant.now());
        // If you want store reason, add column `rejectReason` in ContractOffer
        offerRepo.save(offer);
    }

    @Override
    @Transactional
    public void cancelOfferByClient(Long offerId, Client clientId) {
        ContractOffer offer = offerRepo.findByIdAndClientId(offerId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not yours"));

        if (offer.getStatus() != ContractOfferStatus.SENT) {
            throw new IllegalStateException("Offer cannot be cancelled in status: " + offer.getStatus());
        }

        offer.setStatus(ContractOfferStatus.CANCELLED);
        offerRepo.save(offer);
    }

	@Override
	public ContractOffer setAllOffersPublic() {
		List<ContractOffer> offers = offerRepo.findAll();
        for (ContractOffer offer : offers) {
            offer.setIsPublic(true);  // Make the offer public
        }
        return (ContractOffer) offerRepo.saveAll(offers);  // Save all updated offers
    }
	
	// -----------------------------
    // View methods
    // -----------------------------
	
	@Override
	public List<ContractOfferViewDTO> getOffersByClientUserId(Long userId) {
	    Client client = clientService.getByUserId(userId);
	    List<ContractOffer> offers = offerRepo.findByClientId(client);
	    return offers.stream().map(this::toViewDTO).toList();
	}
	
	@Override
	public List<ContractOfferViewDTO> getOffersByFreelancerId(Long freelancerId) {
	    Freelancer freelancer = freelancerService.getById(freelancerId);
	    List<ContractOffer> offers = offerRepo.findByFreelancerId(freelancer);
	    return offers.stream().map(this::toViewDTO).toList();
	}
	
	@Override
	public ContractOfferViewDTO getOfferById(Long offerId) {
	    ContractOffer offer = offerRepo.findById(offerId)
	            .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
	    return toViewDTO(offer);
	}

    @Override
    public List<ContractOfferViewDTO> getAllOffers() {
        // Map all ContractOffer entities to ContractOfferViewDTO
        return offerRepo.findAll().stream()
            .map(this::toViewDTO)
            .toList();
    }
	
	private ContractOfferViewDTO toViewDTO(ContractOffer offer) {
	    ContractOfferViewDTO dto = new ContractOfferViewDTO();
	    dto.setId(offer.getId());
	    
	    // Client info
	    dto.setClientId(offer.getClientId().getId());
	    dto.setClientName(offer.getClientId().getName());
	    
	    // Freelancer info
	    dto.setFreelancerId(offer.getFreelancerId().getId());
	    dto.setFreelancerName(offer.getFreelancerId().getName());
	    
	    // Gig info
	    dto.setGigId(offer.getSourceId());
	    
	    dto.setDirection(offer.getDirection());
	    dto.setTitle(offer.getTitle());
	    dto.setDescription(offer.getDescription());
	    dto.setTotalBudget(offer.getTotalBudget());
	    dto.setCurrency(offer.getCurrency());
	    dto.setMessage(offer.getMessage());
	    dto.setStatus(offer.getStatus());
	    dto.setCreatedAt(offer.getCreatedAt());
	    dto.setExpiresAt(offer.getExpiresAt());
	    
	    // Get milestones
	    List<ContractOfferMilestone> milestones = offerMsRepo.findByOfferIdOrderByOrderIndexAsc(offer.getId());
	    dto.setMilestones(milestones.stream().map(m -> {
	        ContractOfferMilestoneViewDTO msDto = new ContractOfferMilestoneViewDTO();
	        msDto.setId(m.getId());
	        msDto.setDescription(m.getDescription());
	        msDto.setAmount(m.getAmount());
	        msDto.setOrderIndex(m.getOrderIndex());
	        return msDto;
	    }).toList());
	    
	    return dto;
	}
    
}