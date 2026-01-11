package www.founded.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import www.founded.com.dto.client_freelancer_contract.JobPostCreateDTO;
import www.founded.com.model.client.Client;
import www.founded.com.service.freelancer.FreelancerService;

@Mapper(componentModel = "spring", uses = { FreelancerService.class })
public interface JobsPostMapper {

	JobsPostMapper INSTANCE = Mappers.getMapper(JobsPostMapper.class);

	@Mapping(target = "name", source = "clientId")
	Client toJobPost(JobPostCreateDTO ProposalCreateDTO);

	@Mapping(target = "clientId", source = "name")
	JobPostCreateDTO ProposalCreateDTO(Client toJobPost);
}
