package www.founded.com.config.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authEx) throws IOException {
        
        // 1. Set the response status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 2. Set the WWW-Authenticate header (required for Basic Auth)
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        
        // 3. Customize the response body (e.g., return a JSON error)
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Authentication Required\", \"message\": \"" 
                                   + authEx.getMessage() + "\"}");
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("FoundIT"); 
        super.afterPropertiesSet();
    }
}
