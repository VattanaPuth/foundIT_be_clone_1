package www.founded.com.dto.register;

import lombok.Data;
import www.founded.com.enum_.Status;
import www.founded.com.enum_.security.Role;

@Data
public class UserRegisterResponseDTO {
	private String username;
	private String email;
	private Status status;
	private Role role;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
}
