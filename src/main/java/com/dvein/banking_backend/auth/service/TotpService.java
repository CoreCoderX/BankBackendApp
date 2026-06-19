package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.dto.response.TotpSetupResponse;
import com.dvein.banking_backend.auth.model.TotpSecret;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.TotpSecretRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.CustomException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.QrCodeGenerator;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotpService {

    private final TotpSecretRepository totpSecretRepository;
    private final UserRepository userRepository;
    private final QrCodeGenerator qrCodeGenerator;

    private static final String ISSUER = "DVein Bank";

    @Transactional
    public TotpSetupResponse setupTotp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isTotpEnabled()) {
            throw new CustomException("TOTP is already enabled", "TOTP_003");
        }

        // Generate secret
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();

        // Save or update TOTP secret
        TotpSecret totpSecret = totpSecretRepository.findByUser(user)
                .orElse(TotpSecret.builder()
                        .user(user)
                        .build());

        totpSecret.setSecret(secret);
        totpSecret.setEnabled(false);
        totpSecretRepository.save(totpSecret);

        // Generate QR code
        String qrCode = qrCodeGenerator.generateTotpQRCode(ISSUER, user.getEmail(), secret);

        log.info("TOTP setup initiated for user: {}", user.getEmail());

        return TotpSetupResponse.builder()
                .secret(secret)
                .qrCode(qrCode)
                .issuer(ISSUER)
                .accountName(user.getEmail())
                .build();
    }

    @Transactional
    public void enableTotp(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        TotpSecret totpSecret = totpSecretRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("TOTP not set up. Please setup TOTP first", "TOTP_002"));

        // Verify the code
        if (!verifyTotpCode(totpSecret.getSecret(), code)) {
            throw new CustomException("Invalid TOTP code", "TOTP_001");
        }

        // Enable TOTP
        totpSecret.setEnabled(true);
        totpSecret.setEnabledAt(LocalDateTime.now());
        totpSecretRepository.save(totpSecret);

        user.setTotpEnabled(true);
        userRepository.save(user);

        log.info("TOTP enabled for user: {}", user.getEmail());
    }

    @Transactional
    public void disableTotp(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.isTotpEnabled()) {
            throw new CustomException("TOTP is not enabled", "TOTP_002");
        }

        TotpSecret totpSecret = totpSecretRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("TOTP secret not found", "TOTP_002"));

        // Verify the code before disabling
        if (!verifyTotpCode(totpSecret.getSecret(), code)) {
            throw new CustomException("Invalid TOTP code", "TOTP_001");
        }

        // Disable TOTP
        totpSecret.setEnabled(false);
        totpSecretRepository.save(totpSecret);

        user.setTotpEnabled(false);
        userRepository.save(user);

        log.info("TOTP disabled for user: {}", user.getEmail());
    }

    public boolean verifyTotp(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.isTotpEnabled()) {
            throw new CustomException("TOTP is not enabled", "TOTP_002");
        }

        TotpSecret totpSecret = totpSecretRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("TOTP secret not found", "TOTP_002"));

        boolean valid = verifyTotpCode(totpSecret.getSecret(), code);

        if (valid) {
            totpSecret.setLastUsedAt(LocalDateTime.now());
            totpSecretRepository.save(totpSecret);
        }

        return valid;
    }

    private boolean verifyTotpCode(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        return verifier.isValidCode(secret, code);
    }
}