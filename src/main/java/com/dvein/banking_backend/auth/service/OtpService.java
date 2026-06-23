package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.model.Otp;
import com.dvein.banking_backend.auth.repository.OtpRepository;
import com.dvein.banking_backend.common.enums.OtpType;
import com.dvein.banking_backend.common.exception.InvalidOtpException;
import com.dvein.banking_backend.common.exception.OtpExpiredException;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.util.RandomUtil;
import com.dvein.banking_backend.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final RandomUtil randomUtil;

    @Value("${otp.expiry:300000}")
    private long otpExpiry;

    @Value("${otp.max-retry:3}")
    private int maxRetry;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.resend-cooldown:60000}")
    private long resendCooldown;

    @Transactional
    public void generateAndSendOtp(String email, OtpType otpType) {
        // Check for recent OTP
        Optional<Otp> existingOtp = otpRepository.findTopByEmailAndOtpTypeOrderByCreatedAtDesc(email, otpType);

        if (existingOtp.isPresent()) {
            Otp recentOtp = existingOtp.get();
            long timeSinceCreation = java.time.Duration.between(recentOtp.getCreatedAt(), LocalDateTime.now()).toMillis();

            if (timeSinceCreation < resendCooldown) {
                long remainingSeconds = (resendCooldown - timeSinceCreation) / 1000;
                throw new InvalidRequestException(
                        String.format("Please wait %d seconds before requesting a new OTP", remainingSeconds)
                );
            }
        }

        // Delete old OTPs for this email and type
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        // Generate new OTP
        String code = randomUtil.generateOTP(otpLength);
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(otpExpiry / 1000);

        Otp otp = Otp.builder()
                .email(email)
                .code(code)
                .otpType(otpType)
                .expiresAt(expiryTime)
                .build();

        otpRepository.save(otp);

        // Send OTP via email
        sendOtpEmail(email, code, otpType);

        log.info("OTP generated and sent to email: {} for type: {}", email, otpType);
    }

    @Transactional
    public void verifyOtp(String email, String code, OtpType otpType) {
        Otp otp = otpRepository.findByEmailAndOtpTypeAndVerifiedFalse(email, otpType)
                .orElseThrow(InvalidOtpException::new);

        // Check if OTP is expired
        if (otp.isExpired()) {
            throw new OtpExpiredException();
        }

        // Check retry limit
        if (otp.isMaxRetryReached(maxRetry)) {
            throw new InvalidRequestException("Maximum OTP verification attempts exceeded");
        }

        // Verify code
        if (!otp.getCode().equals(code)) {
            otp.incrementRetryCount();
            otpRepository.save(otp);
            throw new InvalidOtpException();
        }

        // Mark as verified
        otp.markAsVerified();
        otpRepository.save(otp);

        log.info("OTP verified successfully for email: {} and type: {}", email, otpType);
    }

    @Async
    private void sendOtpEmail(String email, String code, OtpType otpType) {
        String subject = getEmailSubject(otpType);
        String message = getEmailMessage(code, otpType);

        emailService.sendOtpEmail(email, subject, code);
    }

    private String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Verify Your Email - DVein Bank";
            case PASSWORD_RESET -> "Reset Your Password - DVein Bank";
            case DEVICE_VERIFICATION -> "Device Verification - DVein Bank";
            case TRANSACTION_VERIFICATION -> "Transaction Verification - DVein Bank";
            case LOGIN_VERIFICATION -> "Login Verification - DVein Bank";
            case TWO_FACTOR_AUTH -> "Two-Factor Authentication - DVein Bank";
        };
    }

    private String getEmailMessage(String code, OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Your email verification code is: " + code;
            case PASSWORD_RESET -> "Your password reset code is: " + code;
            case DEVICE_VERIFICATION -> "Your device verification code is: " + code;
            case TRANSACTION_VERIFICATION -> "Your transaction verification code is: " + code;
            case LOGIN_VERIFICATION -> "Your login verification code is: " + code;
            case TWO_FACTOR_AUTH -> "Your two-factor authentication code is: " + code;
        };
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredAndVerified(LocalDateTime.now());
        log.info("Cleaned up expired and verified OTPs");
    }
}