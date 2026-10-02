package com.dental.dentalbackend.config;

import com.dental.dentalbackend.user.entity.Role;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the initial ADMIN user exists on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Clean up invalid email user if present
        userRepository.findByEmail("safihamza.com").ifPresent(user -> {
            log.info("Removing legacy admin account with invalid email 'safihamza.com'");
            userRepository.delete(user);
        });

        // Ensure safihamza395@gmail.com exists with role ADMIN
        if (!userRepository.existsByEmail("safihamza395@gmail.com")) {
            User admin = new User();
            admin.setFirstName("Hamza");
            admin.setLastName("Safi");
            admin.setEmail("safihamza395@gmail.com");
            admin.setPasswordHash(passwordEncoder.encode("safi1234"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            log.info("Seeded initial ADMIN account: safihamza395@gmail.com");
        }
    }
}
