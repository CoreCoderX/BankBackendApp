package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricService {

    private final UserRepository userRepository;

    @Transactional
    public void toggleBiometric(Long userId, boolean enable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setBiometricEnabled(enable);
        userRepository.save(user);

        log.info("Biometric {} for user: {}", enable ? "enabled" : "disabled", user.getEmail());
    }

    public boolean isBiometricEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return user.isBiometricEnabled();
    }
}