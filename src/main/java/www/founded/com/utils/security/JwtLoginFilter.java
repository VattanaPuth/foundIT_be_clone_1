package www.founded.com.utils.security;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.login.UserLoginRequestDTO;

@RequiredArgsConstructor
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter{
	private final AuthenticationManager authenticationManager;
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			UserLoginRequestDTO loginRequest = mapper.readValue(request.getInputStream(), UserLoginRequestDTO.class);
			Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
			Authentication authenticate = authenticationManager.authenticate(authentication);
			return authenticate;
		} catch (AuthenticationException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse login request", e);
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		try {
			String token = Jwts.builder()
					.setSubject(authResult.getName())
					.setIssuedAt(new Date())
					.claim("authorities", authResult.getAuthorities())
					.setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(7)))
					.setIssuer("FoundIT")
					.signWith(KeyUtils.getKey())
					.compact();
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Authorization", "Bearer " + token);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\":\"Login successful\"}");
		} catch (Exception e) {
			throw new ServletException("Failed to generate JWT token", e);
		}
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write("{\"error\":\"Authentication failed\",\"message\":\"" + failed.getMessage() + "\"}");
	}
}
