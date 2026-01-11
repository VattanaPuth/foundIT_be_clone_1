package www.founded.com.utils.security;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import www.founded.com.service.register.UserRegisterService;

@RequiredArgsConstructor
public class OAuth2LoginHandler implements AuthenticationSuccessHandler{
	private final UserRegisterService userRegister; 

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();      
        String googleSubject = oidcUser.getSubject(); 
        
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .claim("authorities", userRegister.findOrCreateFromGoogle(email, googleSubject))  
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(7)))
                .setIssuer("FoundIT")
                .signWith(KeyUtils.getKey())
                .compact(); 
        
        // Redirect directly to client home with token
        String frontendUrl = "http://localhost:3000/page/client/home?token=" + token;
        response.sendRedirect(frontendUrl);
    }
}
