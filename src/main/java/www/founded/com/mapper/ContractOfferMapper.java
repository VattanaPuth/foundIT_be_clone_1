package www.founded.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import www.founded.com.dto.client_freelancer_contract.ClientOfferCreateDTO;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.service.client.ClientService;
import www.founded.com.service.freelancer.FreelancerService;

@Mapper(componentModel = "spring", uses = {FreelancerService.class, ClientService.class})
public interface ContractOfferMapper {
	ContractOfferMapper INSTANCE = Mappers.getMapper(ContractOfferMapper.class);

    @Mapping(target = "id", source = "clientId")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "user", ignore = true)
	Client toContractOfferClient(ClientOfferCreateDTO toContractOfferDTO);
    
    @Mapping(target = "id", source = "freelancerId")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "user", ignore = true)
    Freelancer toContractOfferFreelancer(ClientOfferCreateDTO toContractOfferDTO);
	
	@Mapping(target = "freelancerId", source = "id")
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "gigId", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "totalBudget", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    @Mapping(target = "message", ignore = true)
	ClientOfferCreateDTO toContractOfferDTO(Freelancer toContractOfferFreelancer);
	
	@Mapping(target = "clientId", source = "id")
    @Mapping(target = "freelancerId", ignore = true)
    @Mapping(target = "gigId", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "totalBudget", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    @Mapping(target = "message", ignore = true)
	ClientOfferCreateDTO toContractOfferDTO(Client toContractOffer);
}
