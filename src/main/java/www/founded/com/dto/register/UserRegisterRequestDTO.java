package www.founded.com.dto.register;

import java.time.LocalDate;

import lombok.Data;
import www.founded.com.enum_.Status;
import www.founded.com.enum_.security.Role;

@Data
public class UserRegisterRequestDTO {
	private String username;
	private String email;
	private String password;
	private Status status;
	private Role role;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private LocalDate createAt;
	private LocalDate updateAt;
}

