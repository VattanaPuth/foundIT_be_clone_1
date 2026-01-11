package www.founded.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import www.founded.com.dto.freelancer.GigFreelancerDTO;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.service.freelancer.FreelancerService;
import www.founded.com.service.freelancer.UserSkillService;

@Mapper(componentModel = "spring", uses = {UserSkillService.class, FreelancerService.class})
public interface GigFreelancerMapper {

	GigFreelancerMapper INSTANCE = Mappers.getMapper(GigFreelancerMapper.class);

	@Mapping(target = "userSkill", source = "userSkillId")
	@Mapping(target = "freelancer", source = "name")
	public GigFreelancer toGigFreelancer(GigFreelancerDTO gigFreelancerDTO);

    @Mapping(target = "userSkillId", source = "userSkill.id")
    @Mapping(target = "name", source = "freelancer.name")
	public GigFreelancerDTO toGigFreelancerDTO(GigFreelancer gigFreelancer);
}
