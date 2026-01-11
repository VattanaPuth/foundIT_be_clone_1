package www.founded.com.utils.security;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import www.founded.com.service.register.UserRegisterService;

public class OAuth2LoginHandler implements AuthenticationSuccessHandler{
	private final UserRegisterService userRegister;
	private final String frontendUrl;
	
	public OAuth2LoginHandler(UserRegisterService userRegister, String frontendUrl) {
		this.userRegister = userRegister;
		this.frontendUrl = frontendUrl;
	} 

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // Handle both OidcUser (OpenID Connect) and OAuth2User
        Object principal = authentication.getPrincipal();
        String email;
        String googleSubject;
        
        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            email = oidcUser.getEmail();
            googleSubject = oidcUser.getSubject();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            email = oauth2User.getAttribute("email");
            googleSubject = oauth2User.getAttribute("sub"); // Google's user ID
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }
        
        // Find or create user and get their authorities
        var user = userRegister.findOrCreateFromGoogle(email, googleSubject);
        var authorities = user.getRole().getAuthorities();
        
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .claim("authorities", authorities)  
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(7)))
                .setIssuer("FoundIT")
                .signWith(KeyUtils.getKey())
                .compact(); 
        
        // Redirect to OAuth2 callback page with token, which will handle auth loading and navigation
        String redirectUrl = frontendUrl + "/page/oauth2/callback?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
