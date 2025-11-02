package com.swaply.swaplybackend.config;

import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.UserRole;
import com.swaply.swaplybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class RootAdminConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RootAdminConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.root-admin.username:admin}")
    private String rootAdminUsername;

    @Value("${app.root-admin.email:admin@swaply.local}")
    private String rootAdminEmail;

    @Value("${app.root-admin.password:ChangeMe123!}")
    private String rootAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUserName(rootAdminUsername).ifPresentOrElse(u -> {}, () -> {
            User admin = new User();
            admin.setUserName(rootAdminUsername);
            admin.setEmail(rootAdminEmail);
            admin.setPassword(passwordEncoder.encode(rootAdminPassword));
            admin.setRole(UserRole.ADMIN);
            admin.setIsActive(true);
            userRepository.save(admin);
        });
    }
}

