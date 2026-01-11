package www.founded.com.service.payment.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.payment.MilestoneCreateDTO;
import www.founded.com.dto.payment.ProjectCreateRequestDTO;
import www.founded.com.enum_.payment.EscrowStatus;
import www.founded.com.enum_.payment.MilestoneStatus;
import www.founded.com.enum_.payment.ProjectStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Milestone;
import www.founded.com.model.payment.Project;
import www.founded.com.repository.payment.EscrowRepository;
import www.founded.com.repository.payment.MilestoneRepository;
import www.founded.com.repository.payment.ProjectRepository;
import www.founded.com.service.client.ClientService;
import www.founded.com.service.freelancer.FreelancerService;
import www.founded.com.service.payment.ProjectService;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{
	private final ProjectRepository projectRepo;
    private final EscrowRepository escrowRepo;
    private final MilestoneRepository milestoneRepo;

    private final ClientService clientService;
    private final FreelancerService freelancerService;

    @Transactional
    public Project createProject(ProjectCreateRequestDTO req) {

        Client client = clientService.getById(req.getClientId());
        Freelancer freelancer = freelancerService.getById(req.getFreelancerId());

        BigDecimal sum = req.getMilestones().stream()
                .map(MilestoneCreateDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(req.getTotalBudget()) != 0) {
            throw new IllegalArgumentException("Milestones total must equal totalBudget.");
        }

        Escrow escrow = new Escrow();
        escrow.setClient(client);
        escrow.setFreelancer(freelancer);
        escrow.setTotalAmount(req.getTotalBudget());
        escrow.setAmountPaidToFreelancer(BigDecimal.ZERO);
        escrow.setAmountRefundedToClient(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.CREATED);
        escrow = escrowRepo.save(escrow);

        for (MilestoneCreateDTO m : req.getMilestones()) {
            Milestone ms = new Milestone();
            ms.setEscrow(escrow);
            ms.setDescription(m.getDescription());
            ms.setAmount(m.getAmount());
            ms.setStatus(MilestoneStatus.NOT_STARTED);
            milestoneRepo.save(ms);
        }

        Project project = new Project();
        project.setClient(client);
        project.setFreelancer(freelancer);
        project.setTitle(req.getTitle());
        project.setDescription(req.getDescription());
        project.setTotalBudget(req.getTotalBudget());
        project.setStatus(ProjectStatus.CREATED);
        project.setEscrow(escrow);

        return projectRepo.save(project);
    }
}
