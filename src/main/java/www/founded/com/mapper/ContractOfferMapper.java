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

    @Mapping(target = "name", source = "clientId")
	Client toContractOfferClient(ClientOfferCreateDTO toContractOfferDTO);
    @Mapping(target = "name", source = "freelancerId")
    Freelancer toContractOfferFreelancer(ClientOfferCreateDTO toContractOfferDTO);
	
	@Mapping(target = "freelancerId", source = "name")
    @Mapping(target = "clientId", source = "name") 
	ClientOfferCreateDTO toContractOfferDTO(Client toContractOffer);
	ClientOfferCreateDTO toContractOfferDTO(Freelancer toContractOfferFreelancer);
	
}
