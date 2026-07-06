package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.model.Mpin;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.MpinRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.CustomException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpinService {

    private final MpinRepository mpinRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Value("${security.mpin.length}")
    private int mpinLength;

    @Value("${security.max-login-attempts}")
    private int maxAttempts;

    @Value("${security.account-lock-duration}")
    private long lockDuration;

    @Transactional
    public void createMpin(Long userId, String mpin, String confirmMpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate MPIN
        validateMpin(mpin, confirmMpin);

        // Check if MPIN already exists
        if (mpinRepository.existsByUser(user)) {
            throw new CustomException("MPIN already exists. Please use change MPIN", "MPIN_003");
        }

        // Hash and save MPIN
        String hashedMpin = encryptionUtil.hashPassword(mpin);

        Mpin mpinEntity = Mpin.builder()
                .user(user)
                .pinHash(hashedMpin)
                .build();

        mpinRepository.save(mpinEntity);

        log.info("MPIN created for user: {}", user.getEmail());
    }

    @Transactional
    public void changeMpin(Long userId, String oldMpin, String newMpin, String confirmMpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Mpin mpin = mpinRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("MPIN not set. Please create MPIN first", "MPIN_002"));

        // Check if locked
        if (mpin.isLocked()) {
            throw new CustomException("MPIN is locked due to multiple failed attempts", "MPIN_001");
        }

        // Verify old MPIN
        if (!encryptionUtil.verifyPassword(oldMpin, mpin.getPinHash())) {
            mpin.incrementFailedAttempts();

            if (mpin.getFailedAttempts() >= maxAttempts) {
                mpin.lock(lockDuration);
            }

            mpinRepository.save(mpin);
            throw new CustomException("Invalid old MPIN", "MPIN_001");
        }

        // Validate new MPIN
        validateMpin(newMpin, confirmMpin);

        // Update MPIN
        String hashedMpin = encryptionUtil.hashPassword(newMpin);
        mpin.setPinHash(hashedMpin);
        mpin.resetFailedAttempts();
        mpinRepository.save(mpin);

        log.info("MPIN changed for user: {}", user.getEmail());
    }

    @Transactional
    public boolean verifyMpin(Long userId, String mpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Mpin mpinEntity = mpinRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("MPIN not set", "MPIN_002"));

        // Check if locked
        if (mpinEntity.isLocked()) {
            throw new CustomException("MPIN is locked due to multiple failed attempts", "MPIN_001");
        }

        // Verify MPIN
        boolean valid = encryptionUtil.verifyPassword(mpin, mpinEntity.getPinHash());

        if (!valid) {
            mpinEntity.incrementFailedAttempts();

            if (mpinEntity.getFailedAttempts() >= maxAttempts) {
                mpinEntity.lock(lockDuration);
            }

            mpinRepository.save(mpinEntity);
            throw new CustomException("Invalid MPIN", "MPIN_001");
        }

        // Reset failed attempts and update last used
        mpinEntity.resetFailedAttempts();
        mpinEntity.setLastUsedAt(LocalDateTime.now());
        mpinRepository.save(mpinEntity);

        return true;
    }

    @Transactional
    public void resetMpin(Long userId, String newMpin, String confirmMpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Mpin mpin = mpinRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("MPIN not set", "MPIN_002"));

        // Validate new MPIN
        validateMpin(newMpin, confirmMpin);

        // Update MPIN
        String hashedMpin = encryptionUtil.hashPassword(newMpin);
        mpin.setPinHash(hashedMpin);
        mpin.resetFailedAttempts();
        mpinRepository.save(mpin);

        log.info("MPIN reset for user: {}", user.getEmail());
    }

    private void validateMpin(String mpin, String confirmMpin) {
        if (mpin == null || mpin.length() != mpinLength) {
            throw new CustomException("MPIN must be " + mpinLength + " digits", "MPIN_001");
        }

        if (!mpin.matches("\\d+")) {
            throw new CustomException("MPIN must contain only digits", "MPIN_001");
        }

        if (!mpin.equals(confirmMpin)) {
            throw new CustomException("MPIN and confirm MPIN do not match", "MPIN_001");
        }
    }
}