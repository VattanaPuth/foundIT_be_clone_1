package www.founded.com.service.admin;

import java.util.List;

import www.founded.com.dto.admin.AdminPostDTO;
import www.founded.com.model.register.UserRegister;

public interface AdminService {
	List<UserRegister> getAllUsers();
	List<AdminPostDTO> getAllPostsByUserRegisterId(Long userRegisterId);
	void deleteUserRegister(Long userRegisterId);
	void deletePost(String postType, Long postId);
}
