package www.founded.com.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import www.founded.com.dto.payment.EscrowResponseDTO;
import www.founded.com.dto.payment.MilestoneResponseDTO;
import www.founded.com.model.payment.Escrow;
import www.founded.com.model.payment.Milestone;
import www.founded.com.service.client.ClientService;
import www.founded.com.service.freelancer.FreelancerService;

@Mapper(componentModel = "spring", uses = {ClientService.class, FreelancerService.class})
public interface EscrowMapper {
	EscrowMapper INSTANCE = Mappers.getMapper(EscrowMapper.class);
	
	@Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "freelancerId", source = "freelancer.id")
    EscrowResponseDTO toResponse(Escrow escrow);

    List<MilestoneResponseDTO> toMilestoneResponses(List<Milestone> milestones);
    MilestoneResponseDTO toMilestoneResponse(Milestone milestone);
	
}
