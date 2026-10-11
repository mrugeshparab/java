package edu.campusnest.config;

import edu.campusnest.data.UserRepository;
import edu.campusnest.domain.AccountStatus;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Re-check account status on each request so suspension also ends existing sessions. */
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public ActiveAccountFilter(UserRepository users){this.users=users;}
    @Override protected void doFilterInternal(HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null&&auth.isAuthenticated()&&!(auth instanceof AnonymousAuthenticationToken)){
            var account=users.findByEmailIgnoreCase(auth.getName()).orElse(null);
            if(account==null||account.getAccountStatus()!=AccountStatus.ACTIVE){
                SecurityContextHolder.clearContext();HttpSession session=request.getSession(false);if(session!=null)session.invalidate();
            }
        }
        chain.doFilter(request,response);
    }
}
