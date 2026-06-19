package com.dvein.banking_backend.common.constant;

public class SuccessMessages {

    // Authentication
    public static final String REGISTRATION_SUCCESS = "Registration successful. Please verify your email.";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    public static final String PASSWORD_CHANGED = "Password changed successfully";
    public static final String PASSWORD_RESET = "Password reset successfully";

    // OTP
    public static final String OTP_SENT = "OTP sent to your email";
    public static final String OTP_VERIFIED = "OTP verified successfully";
    public static final String EMAIL_VERIFIED = "Email verified successfully";

    // Account
    public static final String ACCOUNT_CREATED = "Account created successfully";
    public static final String ACCOUNT_UPDATED = "Account updated successfully";
    public static final String ACCOUNT_ACTIVATED = "Account activated successfully";
    public static final String ACCOUNT_FROZEN = "Account frozen successfully";
    public static final String ACCOUNT_BLOCKED = "Account blocked successfully";
    public static final String ACCOUNT_CLOSED = "Account closed successfully";

    // Customer
    public static final String PROFILE_UPDATED = "Profile updated successfully";
    public static final String CUSTOMER_STATUS_UPDATED = "Customer status updated successfully";

    // Beneficiary
    public static final String BENEFICIARY_ADDED = "Beneficiary added successfully";
    public static final String BENEFICIARY_REMOVED = "Beneficiary removed successfully";

    // Card
    public static final String CARD_GENERATED = "Card generated successfully";
    public static final String CARD_ACTIVATED = "Card activated successfully";
    public static final String CARD_BLOCKED = "Card blocked successfully";
    public static final String CARD_PIN_SET = "Card PIN set successfully";

    // TOTP
    public static final String TOTP_ENABLED = "Two-factor authentication enabled successfully";
    public static final String TOTP_DISABLED = "Two-factor authentication disabled successfully";

    // MPIN
    public static final String MPIN_CREATED = "MPIN created successfully";
    public static final String MPIN_CHANGED = "MPIN changed successfully";

    // Device
    public static final String DEVICE_REGISTERED = "Device registered successfully";
    public static final String DEVICE_REMOVED = "Device removed successfully";

    // KYC
    public static final String KYC_SUBMITTED = "KYC documents submitted successfully";
    public static final String KYC_APPROVED = "KYC approved successfully";
    public static final String KYC_REJECTED = "KYC rejected";
}