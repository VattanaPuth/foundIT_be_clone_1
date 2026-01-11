package www.founded.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import www.founded.com.dto.register.UserRegisterRequestDTO;
import www.founded.com.dto.register.UserRegisterResponseDTO;
import www.founded.com.model.register.UserRegister;

@Mapper(componentModel = "spring")
public interface UserRegisterMapper {
	
	UserRegisterMapper INSTANCE = Mappers.getMapper(UserRegisterMapper.class);
	
	public UserRegister toUserRegisterRequest (UserRegisterRequestDTO userRegisterRequestDTO);
	public UserRegisterRequestDTO toUserRegisterReqDTO (UserRegister userRegisterRequest);
	
	public UserRegister toUserRegisterResponse (UserRegisterResponseDTO userRegisterResponseDTO);
	public UserRegisterResponseDTO toUserRegisterResDTO(UserRegister userRegisterResponse);
	
}
