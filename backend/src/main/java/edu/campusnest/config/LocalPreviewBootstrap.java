package edu.campusnest.config;

import edu.campusnest.data.PropertyImageRepository;
import edu.campusnest.data.PropertyRepository;
import edu.campusnest.data.UserRepository;
import edu.campusnest.domain.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/** Optional local-only restoration for the explicitly supplied preview accounts and listing. */
@Component
@Profile("local")
public class LocalPreviewBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PropertyRepository properties;
    private final PropertyImageRepository images;
    private final PasswordEncoder passwords;

    public LocalPreviewBootstrap(UserRepository users, PropertyRepository properties, PropertyImageRepository images, PasswordEncoder passwords) {
        this.users = users; this.properties = properties; this.images = images; this.passwords = passwords;
    }

    @Override public void run(ApplicationArguments args) throws Exception {
        User owner = account("CAMPUSNEST_LOCAL_OWNER_EMAIL", "CAMPUSNEST_LOCAL_OWNER_PASSWORD", "CampusNest Property Owner", Role.OWNER);
        account("CAMPUSNEST_LOCAL_STUDENT_EMAIL", "CAMPUSNEST_LOCAL_STUDENT_PASSWORD", "CampusNest Student", Role.USER);
        if (owner == null || !"true".equalsIgnoreCase(System.getenv("CAMPUSNEST_LOCAL_RESTORE_HOME"))) return;
        if (!properties.findByOwnerIdOrderByCreatedAtDesc(owner.getId()).isEmpty()) return;

        Property home = new Property(owner, "room", "d", "PG", "panvel", "kamothe", null, null, new BigDecimal("12000"), "yes", null);
        home.approve();
        home = properties.saveAndFlush(home);
        String imageName = System.getenv("CAMPUSNEST_LOCAL_RESTORE_IMAGE");
        if (imageName == null || imageName.isBlank()) return;
        Path imagePath = Path.of("uploads").toAbsolutePath().normalize().resolve(imageName).normalize();
        if (!imagePath.startsWith(Path.of("uploads").toAbsolutePath().normalize()) || !Files.isRegularFile(imagePath)) return;
        images.save(new PropertyImage(home, imageName, "Property photo", "image/png", Files.size(imagePath)));
    }

    private User account(String emailEnv, String passwordEnv, String name, Role role) {
        String email = System.getenv(emailEnv), password = System.getenv(passwordEnv);
        if (email == null || email.isBlank() || password == null || password.length() < 12) return null;
        return users.findByEmailIgnoreCase(email).orElseGet(() -> users.saveAndFlush(new User(name, email.trim().toLowerCase(), passwords.encode(password), null, role)));
    }
}
