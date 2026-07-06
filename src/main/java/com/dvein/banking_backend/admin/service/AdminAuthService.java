package com.dvein.banking_backend.admin.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.exception.DuplicateResourceException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public User createAdmin(String email, String password, UserRole role) {
        // Check if admin already exists
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Admin", "email");
        }

        // Hash password
        String hashedPassword = encryptionUtil.hashPassword(password);

        // Create admin user
        User admin = User.builder()
                .email(email)
                .password(hashedPassword)
                .role(role)
                .active(true)
                .emailVerified(true)
                .build();

        admin = userRepository.save(admin);

        log.info("Admin created: {}", email);

        return admin;
    }

    @Transactional
    public void disableAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));

        if (!admin.getRole().equals(UserRole.ADMIN) && !admin.getRole().equals(UserRole.SUPER_ADMIN)) {
            throw new IllegalArgumentException("User is not an admin");
        }

        admin.setActive(false);
        userRepository.save(admin);

        log.info("Admin disabled: {}", admin.getEmail());
    }

    @Transactional
    public void enableAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));

        if (!admin.getRole().equals(UserRole.ADMIN) && !admin.getRole().equals(UserRole.SUPER_ADMIN)) {
            throw new IllegalArgumentException("User is not an admin");
        }

        admin.setActive(true);
        userRepository.save(admin);

        log.info("Admin enabled: {}", admin.getEmail());
    }

    public List<User> getAllAdmins() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.SUPER_ADMIN))
                .toList();
    }
}