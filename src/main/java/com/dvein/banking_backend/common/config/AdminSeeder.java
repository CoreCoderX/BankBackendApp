package com.dvein.banking_backend.common.config;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Seeds the default SUPER_ADMIN user on first startup.
 * The seed password is read from configuration (env var ADMIN_SEED_PASSWORD).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Value("${admin.seed.password:Admin@123}")
    private String adminSeedPassword;

    @Value("${admin.default.email:admin@banking.com}")
    private String adminEmail;

    @PostConstruct
    public void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(encryptionUtil.hashPassword(adminSeedPassword))
                .role(UserRole.SUPER_ADMIN)
                .active(true)
                .emailVerified(true)
                .build();

        userRepository.save(admin);

        log.warn("SUPER_ADMIN seeded for email: {}. Change the password immediately!", adminEmail);
    }
}
