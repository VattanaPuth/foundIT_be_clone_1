package www.founded.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import www.founded.com.dto.seller.GigSellerDTO;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.service.seller.SellerService;

@Mapper(componentModel = "spring", uses = {SellerService.class})
public interface GigSellerMapper {
	GigSellerMapper INSTANCE = Mappers.getMapper(GigSellerMapper.class);
	
	@Mapping(target = "name", source = "name")
	public GigSeller toGigFreelancer(GigSellerDTO gigFreelancerDTO);
	@Mapping(target = "name", source = "name")
	public GigSellerDTO gigFreelancerDTO(GigSeller toGigFreelancer);
}
