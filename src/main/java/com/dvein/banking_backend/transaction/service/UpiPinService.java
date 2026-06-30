package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import com.dvein.banking_backend.transaction.dto.request.ChangeUpiPinRequest;
import com.dvein.banking_backend.transaction.dto.request.CreateUpiPinRequest;
import com.dvein.banking_backend.transaction.dto.request.VerifyUpiPinRequest;
import com.dvein.banking_backend.transaction.exception.UpiPinLockedException;
import com.dvein.banking_backend.transaction.model.UpiPin;
import com.dvein.banking_backend.transaction.model.UpiProfile;
import com.dvein.banking_backend.transaction.repository.UpiPinRepository;
import com.dvein.banking_backend.transaction.repository.UpiProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpiPinService {

    private final UpiPinRepository upiPinRepository;
    private final UpiProfileRepository upiProfileRepository;
    private final EncryptionUtil encryptionUtil;

    private static final int MAX_PIN_ATTEMPTS = 3;
    private static final long PIN_LOCK_DURATION = 1800000; // 30 minutes

    @Transactional
    public void createUpiPin(CreateUpiPinRequest request, String email) {
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new InvalidRequestException("PIN and confirm PIN do not match");
        }

        UpiProfile profile = upiProfileRepository.findByCustomerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI profile not found"));

        if (upiPinRepository.existsByUpiProfile(profile)) {
            throw new InvalidRequestException("UPI PIN already exists. Use change PIN to update.");
        }

        String pinHash = encryptionUtil.hashPassword(request.getPin());

        UpiPin upiPin = UpiPin.builder()
                .upiProfile(profile)
                .pinHash(pinHash)
                .build();

        upiPinRepository.save(upiPin);
        log.info("UPI PIN created for customer: {}", profile.getCustomer().getId());
    }

    @Transactional
    public void changeUpiPin(ChangeUpiPinRequest request, String email) {
        UpiProfile profile = upiProfileRepository.findByCustomerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI profile not found"));

        UpiPin upiPin = upiPinRepository.findByUpiProfile(profile)
                .orElseThrow(() -> new ResourceNotFoundException("UPI PIN not found. Please create PIN first."));

        if (upiPin.isPinLocked()) {
            throw new UpiPinLockedException("UPI PIN is locked. Please try again later.");
        }

        if (!encryptionUtil.verifyPassword(request.getOldPin(), upiPin.getPinHash())) {
            throw new InvalidRequestException("Old PIN is incorrect");
        }

        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new InvalidRequestException("New PIN and confirm PIN do not match");
        }

        String newPinHash = encryptionUtil.hashPassword(request.getNewPin());
        upiPin.setPinHash(newPinHash);
        upiPin.resetFailedAttempts();

        upiPinRepository.save(upiPin);
        log.info("UPI PIN changed for customer: {}", profile.getCustomer().getId());
    }

    @Transactional
    public void verifyUpiPin(VerifyUpiPinRequest request, String email) {
        UpiProfile profile = upiProfileRepository.findByCustomerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI profile not found"));

        UpiPin upiPin = upiPinRepository.findByUpiProfile(profile)
                .orElseThrow(() -> new ResourceNotFoundException("UPI PIN not found"));

        if (upiPin.isPinLocked()) {
            throw new UpiPinLockedException("UPI PIN is locked due to multiple failed attempts. Please try again later.");
        }

        if (!encryptionUtil.verifyPassword(request.getPin(), upiPin.getPinHash())) {
            upiPin.incrementFailedAttempts();

            if (upiPin.getFailedAttempts() >= MAX_PIN_ATTEMPTS) {
                upiPin.lock(PIN_LOCK_DURATION);
                upiPinRepository.save(upiPin);
                throw new UpiPinLockedException("UPI PIN locked due to multiple failed attempts. Try again in 30 minutes.");
            }

            upiPinRepository.save(upiPin);
            throw new InvalidRequestException(
                    String.format("Invalid UPI PIN. %d attempts remaining.",
                            MAX_PIN_ATTEMPTS - upiPin.getFailedAttempts()));
        }

        upiPin.resetFailedAttempts();
        upiPin.setLastUsedAt(LocalDateTime.now());
        upiPinRepository.save(upiPin);

        log.debug("UPI PIN verified for customer: {}", profile.getCustomer().getId());
    }

    @Transactional
    public void verifyUpiPinByProfile(UpiProfile profile, String pin) {
        UpiPin upiPin = upiPinRepository.findByUpiProfile(profile)
                .orElseThrow(() -> new ResourceNotFoundException("UPI PIN not found"));

        if (upiPin.isPinLocked()) {
            throw new UpiPinLockedException("UPI PIN is locked. Please try again later.");
        }

        if (!encryptionUtil.verifyPassword(pin, upiPin.getPinHash())) {
            upiPin.incrementFailedAttempts();

            if (upiPin.getFailedAttempts() >= MAX_PIN_ATTEMPTS) {
                upiPin.lock(PIN_LOCK_DURATION);
                upiPinRepository.save(upiPin);
                throw new UpiPinLockedException("UPI PIN locked. Try again in 30 minutes.");
            }

            upiPinRepository.save(upiPin);
            throw new InvalidRequestException("Invalid UPI PIN");
        }

        upiPin.resetFailedAttempts();
        upiPin.setLastUsedAt(LocalDateTime.now());
        upiPinRepository.save(upiPin);
    }

    public boolean isPinSet(UpiProfile profile) {
        return upiPinRepository.existsByUpiProfile(profile);
    }

    @Transactional
    public void resetUpiPin(String email, String newPin) {
        UpiProfile profile = upiProfileRepository.findByCustomerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI profile not found"));

        UpiPin upiPin = upiPinRepository.findByUpiProfile(profile)
                .orElseThrow(() -> new ResourceNotFoundException("UPI PIN not found"));

        String newPinHash = encryptionUtil.hashPassword(newPin);
        upiPin.setPinHash(newPinHash);
        upiPin.resetFailedAttempts();

        upiPinRepository.save(upiPin);
        log.info("UPI PIN reset for customer: {}", profile.getCustomer().getId());
    }
}