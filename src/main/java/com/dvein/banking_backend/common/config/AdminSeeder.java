package com.dvein.banking_backend.common.config;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @PostConstruct
    public void seedAdmin() {

        if (userRepository.existsByEmail("admin@banking.com")) {
            return;
        }

        // In production, emailVerified and active are verified
        User admin = User.builder()
                .email("admin@banking.com")
                .password(encryptionUtil.hashPassword("Admin@123"))
                .role(UserRole.SUPER_ADMIN)
                .active(true)
                .emailVerified(true)
                .build();

        userRepository.save(admin);

        System.out.println("SUPER_ADMIN created successfully");
    }
}
