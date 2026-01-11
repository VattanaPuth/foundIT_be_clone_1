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
		
		System.out.println("DEBUG - JwtVerifyFilter: Authorization header = " + AuthorizationHeader);
		System.out.println("DEBUG - JwtVerifyFilter: Request URI = " + request.getRequestURI());
		
		if(AuthorizationHeader == null || !AuthorizationHeader.startsWith("Bearer")) {
			System.out.println("DEBUG - JwtVerifyFilter: No Bearer token found, continuing filter chain");
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = AuthorizationHeader.substring(7);
		token = token.trim();
		
		System.out.println("DEBUG - JwtVerifyFilter: Extracted token (first 50 chars): " + 
			(token.length() > 50 ? token.substring(0, 50) + "..." : token));
		System.out.println("DEBUG - JwtVerifyFilter: Token length: " + token.length());
		System.out.println("DEBUG - JwtVerifyFilter: Token period count: " + token.chars().filter(ch -> ch == '.').count());
		
		try {
			Jws<Claims> claimsJws = Jwts.parserBuilder()
										.setSigningKey(KeyUtils.getKey())
										.build()
										.parseClaimsJws(token);
			
			Claims body = claimsJws.getBody();
			String username = body.getSubject();
			List<Map<String, String>> authorities = (List<Map<String, String>>) body.get("authorities");
			
			System.out.println("DEBUG - JwtVerifyFilter: Successfully parsed JWT");
			System.out.println("DEBUG - JwtVerifyFilter: Username from JWT: " + username);
			System.out.println("DEBUG - JwtVerifyFilter: Authorities: " + authorities);
			
			Set<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
						.map(authority -> new SimpleGrantedAuthority(authority.get("authority")))
						.collect(Collectors.toSet());
			
			Authentication getAuthentication = new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(getAuthentication);
			
			System.out.println("DEBUG - JwtVerifyFilter: Authentication set in SecurityContext");
			
			filterChain.doFilter(request, response);
		}catch(ExpiredJwtException e) {
			logger.info(e.getMessage());
			System.err.println("DEBUG - JwtVerifyFilter: JWT expired - " + e.getMessage());
			throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
		}catch(Exception e) {
			System.err.println("DEBUG - JwtVerifyFilter: JWT parsing error - " + e.getClass().getName() + ": " + e.getMessage());
			throw e;
		}
	}
}
