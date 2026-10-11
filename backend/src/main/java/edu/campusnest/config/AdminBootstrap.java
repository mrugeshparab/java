package edu.campusnest.config;
import edu.campusnest.data.UserRepository;
import edu.campusnest.domain.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminBootstrap implements ApplicationRunner {
 private final UserRepository users; private final PasswordEncoder encoder;
 public AdminBootstrap(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 public void run(ApplicationArguments args){String email=System.getenv("CAMPUSNEST_BOOTSTRAP_ADMIN_EMAIL"),password=System.getenv("CAMPUSNEST_BOOTSTRAP_ADMIN_PASSWORD");
  if(email==null||password==null||email.isBlank()||password.length()<12)return;
  if(users.findByEmailIgnoreCase(email).isEmpty()) users.save(new User("CampusNest Administrator",email.trim().toLowerCase(),encoder.encode(password),null,Role.ADMIN));
 }
}
