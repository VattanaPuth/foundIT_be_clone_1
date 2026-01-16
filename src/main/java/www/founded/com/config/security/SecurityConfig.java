package www.founded.com.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import www.founded.com.enum_.security.Permission;
import www.founded.com.enum_.security.Role;
import www.founded.com.service.register.UserRegisterService;
import www.founded.com.service.security.db.UserDetailsServiceImpl;
import www.founded.com.utils.security.JwtLoginFilter;
import www.founded.com.utils.security.JwtVerifyFilter;
import www.founded.com.utils.security.OAuth2LoginHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{
	private final CustomBasicAuthenticationEntryPoint authenticationEntryPoint;
	private final UserDetailsServiceImpl userDetailsService;
	private final UserRegisterService userRegister;
	private final AuthenticationConfiguration authenticationConfiguration;
	private final org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource;
	
	@Value("${frontend.url:http://localhost:3000}")
	private String frontendUrl;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// Configure JWT login filter to only process /login endpoint
		JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager());
		jwtLoginFilter.setFilterProcessesUrl("/login");
		
		return http
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.csrf(csrf -> csrf.disable())
			.addFilter(jwtLoginFilter)
			.addFilterAfter(new JwtVerifyFilter(), JwtLoginFilter.class)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
			.authorizeHttpRequests(rq -> rq
			  .requestMatchers("/", "/login**", "/oauth2/**", "/css/**", "/js/**").permitAll()
			      .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
			      .requestMatchers(HttpMethod.POST, "/login", "/api/login", "/api/login/email", "/api/check-auth", "/register", "/register/**").permitAll()                 
			      .requestMatchers(HttpMethod.PUT, "/api/user/update-role").authenticated()                        
			      // Allow all WebSocket upgrade requests to /chat and /chat/**
			      .requestMatchers("/chat", "/chat/**").permitAll()
			      .requestMatchers(HttpMethod.POST, "/ekyc/**").hasAnyRole(Role.CLIENT.name(), Role.FREELANCER.name(), Role.SELLER.name())
			      .requestMatchers(HttpMethod.GET,"/gigs/**").hasAuthority(Permission.READ_GIG.getDescripton())
					  .requestMatchers(HttpMethod.POST,"/gigs/**").hasAuthority(Permission.CREATE_GIG.getDescripton())
					  .requestMatchers(HttpMethod.PUT, "/gigs/**").hasAuthority(Permission.UPDATE_GIG.getDescripton())
					  .requestMatchers(HttpMethod.DELETE, "/gigs/**").hasAuthority(Permission.DELETE_GIG.getDescripton())
					  .requestMatchers("/gigs/**").hasAnyRole(Role.CLIENT.name(), Role.FREELANCER.name(), Role.SELLER.name())
					  .requestMatchers(HttpMethod.GET, "/admin/**").hasAuthority(Permission.READ_USER.getDescripton())
					  .requestMatchers(HttpMethod.DELETE, "/admin/**").hasAuthority(Permission.DELETE_USER.getDescripton())
					  .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
					  .anyRequest().authenticated())
				.httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
				.oauth2Login(oauth2 -> oauth2
			            .successHandler(new OAuth2LoginHandler(userRegister, frontendUrl))
			        )
				.build();
	}
	
	@Bean
	public AuthenticationProvider getAuthenticationProvider() {
		DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
		dao.setPasswordEncoder(new BCryptPasswordEncoder(14));
		return dao;
	}
	
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(14);
    }
}
