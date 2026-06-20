package com.dvein.banking_backend.notification.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailTemplates {

    public static String buildRegistrationEmail(String firstName) {
        return "Welcome to DVein Bank, " + firstName + "!\n\n" +
                "Your account has been created successfully.\n" +
                "Please verify your email to complete registration.\n\n" +
                "Best regards,\n" +
                "DVein Bank Team";
    }

    public static String buildOtpEmail(String otp, String purpose) {
        return "Your OTP for " + purpose + " is: " + otp + "\n\n" +
                "This OTP is valid for 5 minutes. Do not share it with anyone.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "DVein Bank Team";
    }

    public static String buildPasswordResetEmail(String resetLink) {
        return "We received a request to reset your password.\n\n" +
                "Click the link below to reset your password:\n" + resetLink + "\n\n" +
                "Link expires in 1 hour.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "DVein Bank Team";
    }

    public static String buildSecurityAlertEmail(String alertMessage, String ipAddress, String deviceInfo) {
        return "SECURITY ALERT\n\n" +
                "Unusual activity detected on your DVein Bank account.\n\n" +
                "Activity: " + alertMessage + "\n" +
                "IP Address: " + ipAddress + "\n" +
                "Device: " + deviceInfo + "\n\n" +
                "If this was not you, please change your password immediately and contact our support team.\n\n" +
                "DVein Bank Team";
    }

    public static String buildWelcomeEmail(String userName) {
        return "Welcome to DVein Bank, " + userName + "!\n\n" +
                "Your account has been created successfully.\n\n" +
                "Start banking with DVein Bank today!\n\n" +
                "Features:\n" +
                "- Secure Account Management\n" +
                "- Multiple Account Types\n" +
                "- Advanced Security Features\n" +
                "- 24/7 Customer Support\n\n" +
                "If you have any questions, please contact our support team.\n\n" +
                "Best regards,\n" +
                "DVein Bank Team";
    }

    public static String buildTransactionAlertEmail(String transactionType, String amount, String date) {
        return "Transaction Alert\n\n" +
                "A " + transactionType + " transaction has been processed.\n" +
                "Amount: " + amount + "\n" +
                "Date: " + date + "\n\n" +
                "If you did not authorize this transaction, please contact us immediately.\n\n" +
                "DVein Bank Team";
    }

    public static String buildKycApprovalEmail(String userName) {
        return "Great News!\n\n" +
                "Your KYC verification has been approved.\n" +
                "You can now enjoy all the features of your DVein Bank account.\n\n" +
                "Thank you,\n" +
                "DVein Bank Team";
    }

    public static String buildKycRejectionEmail(String userName, String reason) {
        return "KYC Verification Status\n\n" +
                "Unfortunately, your KYC verification could not be approved.\n" +
                "Reason: " + reason + "\n\n" +
                "Please resubmit with correct documents.\n\n" +
                "For assistance, contact our support team.\n\n" +
                "DVein Bank Team";
    }

    public static String buildCardActivationEmail(String cardType, String cardLastFourDigits) {
        return "Card Activation Successful\n\n" +
                "Your " + cardType + " card ending in " + cardLastFourDigits + " has been activated.\n\n" +
                "You can now use your card for transactions.\n\n" +
                "DVein Bank Team";
    }

    public static String buildAccountNotificationEmail(String accountNumber, String message) {
        return "Account Notification\n\n" +
                "Account: " + accountNumber + "\n" +
                "Message: " + message + "\n\n" +
                "For more details, log in to your account.\n\n" +
                "DVein Bank Team";
    }
}