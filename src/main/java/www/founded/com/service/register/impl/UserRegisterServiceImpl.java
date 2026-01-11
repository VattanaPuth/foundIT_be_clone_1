package www.founded.com.service.register.impl;

import java.util.Date;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.Status;
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
	@Transactional
	public UserRegister findOrCreateFromGoogle(String email, String googleSubject) {
		return registerRepository.findByEmail(email)
				.orElseGet(() -> {
					UserRegister user = new UserRegister();
						user.setEmail(email);
						user.setUsername(email);
	                    user.setGoogleSubject(googleSubject);
	                    user.setStatus(Status.ACTIVE); // Set default status
	                    user.setAccountNonExpired(true);
	                    user.setAccountNonLocked(true);
	                    user.setCredentialsNonExpired(true);
	                    user.setEnabled(true);
	                    user.setCreateAt(new Date());
	                    
	                    Role role = decideRoleForGoogleUser(email);
	                    user.setRole(role);
	                    
	                    // Save and flush to ensure immediate database commit
					UserRegister savedUser = registerRepository.save(user);
					registerRepository.flush();
					return savedUser;
				});
	}
	
	private Role decideRoleForGoogleUser(String email) {
	    // Default to CLIENT for all OAuth2 users
	    return Role.CLIENT;
	}
}
