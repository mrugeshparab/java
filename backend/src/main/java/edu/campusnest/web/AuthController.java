package edu.campusnest.web;
import edu.campusnest.data.UserRepository;
import edu.campusnest.domain.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager auth; private final SecurityContextRepository contexts; private final CsrfTokenRepository csrfTokens;
 public AuthController(UserRepository users,PasswordEncoder encoder,AuthenticationManager auth,SecurityContextRepository contexts,CsrfTokenRepository csrfTokens){this.users=users;this.encoder=encoder;this.auth=auth;this.contexts=contexts;this.csrfTokens=csrfTokens;}
 public record Registration(@NotBlank @Size(max=120) String fullName,@Email @NotBlank String email,@Size(min=12,max=72) String password,@Size(max=30) String phone,@NotNull Role role){}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 public record Account(Long id,String fullName,String email,String phone,String role,String accountStatus,Instant createdAt){}
 private Account dto(User u){return new Account(u.getId(),u.getFullName(),u.getEmail(),u.getPhone(),u.getRole().name(),u.getAccountStatus().name(),u.getCreatedAt());}
 @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken t){return Map.of("token",t.getToken());}
 @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody Registration r){
   if(r.role()==Role.ADMIN) throw new IllegalArgumentException("Public registration cannot create administrators.");
   if(users.findByEmailIgnoreCase(r.email()).isPresent()) return ResponseEntity.status(409).body(Map.of("code","EMAIL_IN_USE","message","An account already uses this email."));
   var u=users.save(new User(r.fullName().trim(),r.email().trim().toLowerCase(),encoder.encode(r.password()),r.phone(),r.role()));return ResponseEntity.status(201).body(dto(u));
 }
 @PostMapping("/login") public Account login(@Valid @RequestBody Login l,HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response){var authenticated=auth.authenticate(new UsernamePasswordAuthenticationToken(l.email().trim().toLowerCase(),l.password()));request.getSession(true);request.changeSessionId();csrfTokens.saveToken(null,request,response);var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(authenticated);SecurityContextHolder.setContext(context);contexts.saveContext(context,request,response);return dto(users.findByEmailIgnoreCase(l.email()).orElseThrow());}
 @GetMapping("/me") public ResponseEntity<?> me(Authentication a){if(a==null)return ResponseEntity.status(401).body(Map.of("code","UNAUTHENTICATED","message","Sign in is required."));return ResponseEntity.ok(dto(users.findByEmailIgnoreCase(a.getName()).orElseThrow()));}
}
