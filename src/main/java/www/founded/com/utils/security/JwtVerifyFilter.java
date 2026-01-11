package www.founded.com.utils.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import www.founded.com.exception.ApiException;

public class JwtVerifyFilter extends OncePerRequestFilter{

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String AuthorizationHeader = request.getHeader("Authorization");
		
		if(AuthorizationHeader == null || !AuthorizationHeader.startsWith("Bearer")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = AuthorizationHeader.substring(7);
		token = token.trim();
		
		try {
			Jws<Claims> claimsJws = Jwts.parserBuilder()
										.setSigningKey(KeyUtils.getKey())
										.build()
										.parseClaimsJws(token);
			
			Claims body = claimsJws.getBody();
			String username = body.getSubject();
			List<Map<String, String>> authorities = (List<Map<String, String>>) body.get("authorities");
			
			Set<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
						.map(authority -> new SimpleGrantedAuthority(authority.get("authority")))
						.collect(Collectors.toSet());
			
			Authentication getAuthentication = new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(getAuthentication);
			
			filterChain.doFilter(request, response);
		}catch(ExpiredJwtException e) {
			logger.info(e.getMessage());
			throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
