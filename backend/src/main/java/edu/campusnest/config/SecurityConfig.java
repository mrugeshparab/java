package edu.campusnest.config;
import edu.campusnest.data.UserRepository;
import edu.campusnest.domain.AccountStatus;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService userDetailsService(UserRepository users){return email->{var u=users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("Account not found"));
   if(u.getAccountStatus()!=AccountStatus.ACTIVE) throw new DisabledException("Account is suspended");
   return User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles(u.getRole().name()).build();};}
 @Bean AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration c) throws Exception{return c.getAuthenticationManager();}
 @Bean SecurityContextRepository securityContextRepository(){return new HttpSessionSecurityContextRepository();}
 @Bean CsrfTokenRepository csrfTokenRepository(){return new HttpSessionCsrfTokenRepository();}
 @Bean SecurityFilterChain security(HttpSecurity http,UserRepository users,CsrfTokenRepository csrf) throws Exception {
   http.csrf(c->c.csrfTokenRepository(csrf).csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler()));
   http.authorizeHttpRequests(a->a.requestMatchers("/","/index.html","/admin.html","/assets/**","/api/auth/**","/api/properties","/api/properties/**").permitAll().requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/owner/**").hasRole("OWNER").requestMatchers("/api/user/**").hasRole("USER").requestMatchers("/api/**").denyAll().anyRequest().permitAll());
   http.formLogin(f->f.disable()).httpBasic(b->b.disable());
   http.exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"code\":\"UNAUTHENTICATED\",\"message\":\"Sign in is required.\"}");}).accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json");res.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"You are not allowed to perform this action.\"}");}));
   http.logout(l->l.logoutUrl("/api/auth/logout").logoutSuccessHandler((req,res,auth)->res.setStatus(204)));
   http.addFilterBefore(new ActiveAccountFilter(users),org.springframework.security.web.access.intercept.AuthorizationFilter.class);
   return http.build();
 }
}
