package www.founded.com.service.payment.client_freelancer_contract.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client_freelancer_contract.ProposalCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ProposalDetailDTO;
import www.founded.com.dto.client_freelancer_contract.ProposalMilestoneCreateDTO;
import www.founded.com.dto.client_freelancer_contract.ProposalMilestoneDTO;
import www.founded.com.enum_.client_freelancer_contract.ContractOfferStatus;
import www.founded.com.enum_.client_freelancer_contract.JobPostStatus;
import www.founded.com.enum_.client_freelancer_contract.OfferDirection;
import www.founded.com.enum_.client_freelancer_contract.OfferSourceType;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.payment.MilestoneStatus;
import www.founded.com.enum_.payment.ProjectStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Milestone;
import www.founded.com.model.payment.Project;
import www.founded.com.model.payment.client_freelancer_contract.ContractOffer;
import www.founded.com.model.payment.client_freelancer_contract.ContractOfferMilestone;
import www.founded.com.model.payment.client_freelancer_contract.JobPost;
import www.founded.com.repository.client_freelancer_contract.ContractOfferMilestoneRepository;
import www.founded.com.repository.client_freelancer_contract.ContractOfferRepository;
import www.founded.com.repository.client_freelancer_contract.JobPostRepository;
import www.founded.com.repository.freelancer.GigFreelancerRepository;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.payment.MilestoneRepository;
import www.founded.com.repository.payment.ProjectRepository;
import www.founded.com.service.client.ClientService;
import www.founded.com.service.freelancer.FreelancerService;
import www.founded.com.service.payment.client_freelancer_contract.ProposalService;
@Service
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {

    private final JobPostRepository jobPostRepo;

    private final ContractOfferRepository offerRepo;
    private final ContractOfferMilestoneRepository offerMsRepo;

    private final ProjectRepository projectRepo;
    private final EscrowRepository escrowRepo;
    private final MilestoneRepository milestoneRepo;

    private final ClientService clientService;
    private final FreelancerService freelancerService;
    private final GigFreelancerRepository gigRepo;

    // -----------------------------
    // GET methods with full details
    // -----------------------------
    @Override
    public ProposalDetailDTO getProposalDetailById(Long proposalId) {
        ContractOffer offer = offerRepo.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        return mapToProposalDetail(offer);
    }

    @Override
    public List<ProposalDetailDTO> getProposalDetailsForClient(Long clientId) {
        List<ContractOffer> offers = offerRepo.findByClientId_IdAndDirection(clientId, OfferDirection.FREELANCER_TO_CLIENT);
        return offers.stream()
                .map(this::mapToProposalDetail)
                .collect(Collectors.toList());
    }

    private ProposalDetailDTO mapToProposalDetail(ContractOffer offer) {
        ProposalDetailDTO dto = new ProposalDetailDTO();
        dto.setId(offer.getId());
        dto.setJobPostId(offer.getSourceId());
        dto.setClientId(offer.getClientId() != null ? offer.getClientId().getId() : null);
        dto.setFreelancerId(offer.getFreelancerId() != null ? offer.getFreelancerId().getId() : null);
        dto.setCoverLetter(offer.getMessage());
        dto.setProposedBudget(offer.getTotalBudget());
        dto.setCurrency(offer.getCurrency());
        dto.setDeliveryDays(offer.getDeliveryDays());
        dto.setStatus(mapContractOfferStatusToProposalStatus(offer.getStatus()));
        dto.setProjectId(offer.getProjectId());
        dto.setCreatedAt(offer.getCreatedAt());

        // Fetch job title
        if (offer.getSourceId() != null) {
            jobPostRepo.findById(offer.getSourceId()).ifPresent(job -> {
                dto.setJobTitle(job.getTitle());
            });
        }

        // Fetch freelancer details
        if (offer.getFreelancerId() != null) {
            try {
                Freelancer freelancer = freelancerService.getById(offer.getFreelancerId().getId());
                dto.setFreelancerName(freelancer.getName());

                // Try to get gig details for more info
                List<GigFreelancer> gigs = gigRepo.findAll();
                gigs.stream()
                    .filter(g -> g.getFreelancer().getId().equals(freelancer.getId()))
                    .findFirst()
                    .ifPresent(gig -> {
                        dto.setFreelancerBio(gig.getShortBio());
                        dto.setFreelancerSkill(gig.getUserSkill() != null ? gig.getUserSkill().getSkill() : null);
                        
                        // Generate avatar URL
                        String name = freelancer.getName() != null ? freelancer.getName() : "User";
                        String encodedName = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
                        dto.setFreelancerAvatar("https://ui-avatars.com/api/?name=" + encodedName + "&size=128&background=random&color=fff&bold=true");
                    });
            } catch (Exception e) {
                // Freelancer not found, continue
            }
        }

        // Fetch milestones
        List<ContractOfferMilestone> offerMilestones = offerMsRepo.findByOfferIdOrderByOrderIndexAsc(offer.getId());
        List<ProposalMilestoneDTO> milestoneDTOs = offerMilestones.stream()
                .map(this::mapToMilestoneDTO)
                .collect(Collectors.toList());
        dto.setMilestones(milestoneDTOs);

        return dto;
    }

    private ProposalMilestoneDTO mapToMilestoneDTO(ContractOfferMilestone milestone) {
        ProposalMilestoneDTO dto = new ProposalMilestoneDTO();
        dto.setId(milestone.getId());
        dto.setOfferId(milestone.getOfferId());
        dto.setDescription(milestone.getDescription());
        dto.setAmount(milestone.getAmount());
        dto.setOrderIndex(milestone.getOrderIndex());
        return dto;
    }

    // -----------------------------
    // GET methods (basic)
    // -----------------------------
    @Override
    public www.founded.com.model.payment.freelancer_client_contract.Proposal getProposalById(Long proposalId) {
        return offerRepo.findById(proposalId)
                .map(this::mapToProposal)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
    }

    @Override
    public List<www.founded.com.model.payment.freelancer_client_contract.Proposal> getProposalsForClient(Long clientId) {
        List<ContractOffer> offers = offerRepo.findByClientId_IdAndDirection(clientId, OfferDirection.FREELANCER_TO_CLIENT);
        return offers.stream()
                .map(this::mapToProposal)
                .toList();
    }

    @Override
    public List<www.founded.com.model.payment.freelancer_client_contract.Proposal> getProposalsForFreelancer(Long freelancerId) {
        List<ContractOffer> offers = offerRepo.findByFreelancerId_IdAndDirection(freelancerId, OfferDirection.FREELANCER_TO_CLIENT);
        return offers.stream()
                .map(this::mapToProposal)
                .toList();
    }

    private www.founded.com.model.payment.freelancer_client_contract.Proposal mapToProposal(ContractOffer offer) {
        www.founded.com.model.payment.freelancer_client_contract.Proposal proposal = 
            new www.founded.com.model.payment.freelancer_client_contract.Proposal();
        proposal.setId(offer.getId());
        proposal.setJobPostId(offer.getSourceId());
        proposal.setClientId(offer.getClientId() != null ? offer.getClientId().getId() : null);
        proposal.setFreelancerId(offer.getFreelancerId() != null ? offer.getFreelancerId().getId() : null);
        proposal.setCoverLetter(offer.getMessage());
        proposal.setProposedBudget(offer.getTotalBudget());
        proposal.setCurrency(offer.getCurrency());
        proposal.setDeliveryDays(offer.getDeliveryDays());
        proposal.setStatus(mapContractOfferStatusToProposalStatus(offer.getStatus()));
        proposal.setProjectId(offer.getProjectId());
        proposal.setCreatedAt(offer.getCreatedAt());
        return proposal;
    }

    private www.founded.com.enum_.client_freelancer_contract.ProposalStatus mapContractOfferStatusToProposalStatus(
            ContractOfferStatus status) {
        return switch (status) {
            case SENT -> www.founded.com.enum_.client_freelancer_contract.ProposalStatus.PENDING;
            case ACCEPTED -> www.founded.com.enum_.client_freelancer_contract.ProposalStatus.ACCEPTED;
            case REJECTED -> www.founded.com.enum_.client_freelancer_contract.ProposalStatus.REJECTED;
            case WITHDRAWN -> www.founded.com.enum_.client_freelancer_contract.ProposalStatus.WITHDRAWN;
            default -> www.founded.com.enum_.client_freelancer_contract.ProposalStatus.PENDING;
        };
    }

    // -----------------------------
    // 1) Freelancer sends proposal
    // -----------------------------
    @Override
    @Transactional
    public Long sendProposal(ProposalCreateDTO dto) {

        JobPost job = jobPostRepo.findById(dto.getJobPostId())
                .orElseThrow(() -> new IllegalArgumentException("JobPost not found"));

        // ensure freelancer exists
        freelancerService.getById(dto.getFreelancerId());

        // milestone sum validation
        BigDecimal sum = dto.getMilestones().stream()
                .map(ProposalMilestoneCreateDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(dto.getProposedBudget()) != 0) {
            throw new IllegalArgumentException("Proposal milestones total must equal proposedBudget.");
        }

        ContractOffer offer = new ContractOffer();
        offer.setClientId(job.getClientId());
        offer.setDirection(OfferDirection.FREELANCER_TO_CLIENT);
        offer.setSourceType(OfferSourceType.JOB_POST);
        offer.setSourceId(job.getId());

        offer.setTitle(job.getTitle());
        offer.setDescription(job.getDescription());

        offer.setTotalBudget(dto.getProposedBudget());
        offer.setCurrency(dto.getCurrency() == null ? job.getCurrency() : dto.getCurrency());
        offer.setDeliveryDays(dto.getDeliveryDays());
        offer.setMessage(dto.getMessage());

        offer.setStatus(ContractOfferStatus.SENT);
        offer = offerRepo.save(offer);

        for (int i = 0; i < dto.getMilestones().size(); i++) {
            ProposalMilestoneCreateDTO m = dto.getMilestones().get(i);
            ContractOfferMilestone ms = new ContractOfferMilestone();
            ms.setOfferId(offer.getId());
            ms.setDescription(m.getDescription());
            ms.setAmount(m.getAmount());
            ms.setOrderIndex(i + 1);
            offerMsRepo.save(ms);
        }

        return offer.getId();
    }

    // -----------------------------
    // 2) Client accepts proposal
    // → create Project + Escrow + Milestones
    // -----------------------------
    @Override
    @Transactional
    public Project acceptProposal(Long offerId, Client clientId) {

        ContractOffer offer = offerRepo.findByIdAndClientId(offerId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not yours"));

        if (offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(Instant.now())) {
            offer.setStatus(ContractOfferStatus.EXPIRED);
            offerRepo.save(offer);
            throw new IllegalStateException("Offer expired");
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

        // load real entities for Project/Escrow
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

        // create milestones from offer
        List<ContractOfferMilestone> offerMilestones = offerMsRepo.findByOfferIdOrderByOrderIndexAsc(offer.getId());
        for (ContractOfferMilestone m : offerMilestones) {
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
        project.setDescription(offer.getMessage()); // use message as “agreement note”
        project.setTotalBudget(offer.getTotalBudget());

        // link back (optional fields)
        project.setProposalId(offer.getId()); // if you added proposalId OR you can reuse hireRequestId
        // or: project.setHireRequestId(offer.getId());

        project = projectRepo.save(project);

        // update offer
        offer.setStatus(ContractOfferStatus.ACCEPTED);
        offer.setAcceptedAt(Instant.now());
        offer.setProjectId(project.getId());
        offerRepo.save(offer);

        // optionally close job post
        if (offer.getSourceType() == OfferSourceType.JOB_POST && offer.getSourceId() != null) {
            JobPost job = jobPostRepo.findById(offer.getSourceId()).orElse(null);
            if (job != null) {
                job.setStatus(JobPostStatus.CLOSED);
                jobPostRepo.save(job);
            }
        }

        return project;
    }

    @Override
    @Transactional
    public void rejectProposal(Long offerId, Client clientId, String reason) {
        ContractOffer offer = offerRepo.findByIdAndClientId(offerId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not yours"));
        if (offer.getStatus() != ContractOfferStatus.SENT) {
            throw new IllegalStateException("Cannot reject in status: " + offer.getStatus());
        }
        offer.setStatus(ContractOfferStatus.REJECTED);
        offer.setRejectedAt(Instant.now());
        offerRepo.save(offer);
    }

    @Override
    @Transactional
    public void withdrawProposal(Long offerId, Freelancer freelancerId) {
        ContractOffer offer = offerRepo.findByIdAndFreelancerId(offerId, freelancerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not yours"));
        if (offer.getStatus() != ContractOfferStatus.SENT) {
            throw new IllegalStateException("Cannot withdraw in status: " + offer.getStatus());
        }
        offer.setStatus(ContractOfferStatus.WITHDRAWN);
        offerRepo.save(offer);
    }

}