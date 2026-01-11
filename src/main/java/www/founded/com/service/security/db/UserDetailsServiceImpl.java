package www.founded.com.service.security.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import www.founded.com.model.register.UserRegister;
import www.founded.com.repository.register.UserRegisterRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
	
	@Autowired
	private UserRegisterRepository registerRepository;
	
	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		// Try to find user by email first (since the login form uses email)
		UserRegister user = registerRepository.findByEmail(usernameOrEmail)
				.orElseGet(() -> {
					// If not found by email, try by username
					return registerRepository.findByUsername(usernameOrEmail)
						.orElseThrow(() -> new UsernameNotFoundException(
							"User not found with username or email: " + usernameOrEmail));
				});
		
		return AuthUserDetails.builder()
				.username(user.getUsername())
				.password(user.getPassword())
				.authorities(user.getRole().getAuthorities())
				.accountNonExpired(user.isAccountNonExpired())
				.accountNonLocked(user.isAccountNonLocked())
				.credentialsNonExpired(user.isCredentialsNonExpired())
				.enabled(user.isEnabled())
			.build();
	}
}
