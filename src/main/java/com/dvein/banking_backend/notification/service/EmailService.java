package com.dvein.banking_backend.notification.service;

import com.dvein.banking_backend.notification.template.EmailTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.from-name}")
    private String fromName;

    @Async
    public void sendOtpEmail(String to, String subject, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(EmailTemplates.buildOtpEmail(otp, subject));

            mailSender.send(message);

            log.info("OTP email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
        }
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Async
    public void sendVerificationEmail(String to, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Email Verification - DVein Bank");
            message.setText(buildVerificationEmailBody(verificationLink));

            mailSender.send(message);

            log.info("Verification email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Password Reset - DVein Bank");
            message.setText(EmailTemplates.buildPasswordResetEmail(resetLink));

            mailSender.send(message);

            log.info("Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    @Async
    public void sendSecurityAlert(String to, String alertMessage, String ipAddress, String deviceInfo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Security Alert - DVein Bank");
            message.setText(EmailTemplates.buildSecurityAlertEmail(alertMessage, ipAddress, deviceInfo));

            mailSender.send(message);

            log.info("Security alert email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send security alert email to: {}", to, e);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to DVein Bank");
            message.setText(EmailTemplates.buildWelcomeEmail(userName));

            mailSender.send(message);

            log.info("Welcome email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    public void sendKycApprovalEmail(String to, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("KYC Approved - DVein Bank");
            message.setText(EmailTemplates.buildKycApprovalEmail(userName));

            mailSender.send(message);

            log.info("KYC approval email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send KYC approval email to: {}", to, e);
        }
    }

    @Async
    public void sendCardActivationEmail(String to, String cardType, String cardLastFourDigits) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Card Activated - DVein Bank");
            message.setText(EmailTemplates.buildCardActivationEmail(cardType, cardLastFourDigits));

            mailSender.send(message);

            log.info("Card activation email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send card activation email to: {}", to, e);
        }
    }

    private String buildVerificationEmailBody(String link) {
        return "Thank you for registering with DVein Bank.\n\n" +
                "Please click the link below to verify your email:\n" + link + "\n\n" +
                "Link expires in 24 hours.";
    }
}