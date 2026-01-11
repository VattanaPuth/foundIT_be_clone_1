package www.founded.com.service.register.impl;

import java.util.Date;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.security.Role;
import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.service.register.UserRegisterService;

@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService{
	private final UserRegisterRepository registerRepository;
	
	private BCryptPasswordEncoder bcryptPassword = new BCryptPasswordEncoder();
	
	@Override
	public UserRegister saveUser(UserRegister userRegister) {
		userRegister.setPassword(bcryptPassword.encode(userRegister.getPassword()));
		return registerRepository.save(userRegister);
	}

	@Override // oAuth2
	public UserRegister findOrCreateFromGoogle(String email, String googleSubject) {
		return registerRepository.findByEmail(email)
				.orElseGet(() -> {
					UserRegister user = new UserRegister();
						user.setEmail(email);
						user.setUsername(email);
	                    user.setGoogleSubject(googleSubject);
	                    user.setStatus(user.getStatus());
	                    user.setAccountNonExpired(true);
	                    user.setAccountNonLocked(true);
	                    user.setCredentialsNonExpired(true);
	                    user.setEnabled(true);
	                    user.setCreateAt(new Date());
	                    
	                    Role role = decideRoleForGoogleUser(email);
	                    user.setRole(role);
					return registerRepository.save(user);
				});
	}
	
	private Role decideRoleForGoogleUser(String email) {
	    if (email.endsWith("@gmail.com")) {
	    	return Role.CLIENT;
	    }
		return null;
	}
}
