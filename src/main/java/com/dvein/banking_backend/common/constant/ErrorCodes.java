package com.dvein.banking_backend.common.constant;

public class ErrorCodes {

    // Authentication Errors (AUTH_XXX)
    public static final String AUTH_001 = "Invalid credentials";
    public static final String AUTH_002 = "Account locked due to multiple failed attempts";
    public static final String AUTH_003 = "Token expired";
    public static final String AUTH_004 = "Invalid token";
    public static final String AUTH_005 = "Unauthorized access";
    public static final String AUTH_006 = "Session expired";
    public static final String AUTH_007 = "Account not activated";
    public static final String AUTH_008 = "Email not verified";
    public static final String AUTH_009 = "Pre-authentication session expired";
    public static final String AUTH_010 = "Device verification required";
    public static final String AUTH_011 = "TOTP verification required";
    public static final String AUTH_012 = "Invalid pre-authentication token";
    public static final String AUTH_013 = "Authentication incomplete";

    // OTP Errors (OTP_XXX)
    public static final String OTP_001 = "Invalid OTP";
    public static final String OTP_002 = "OTP expired";
    public static final String OTP_003 = "Maximum retry limit exceeded";
    public static final String OTP_004 = "OTP not found";
    public static final String OTP_005 = "Please wait before requesting new OTP";

    // User Errors (USER_XXX)
    public static final String USER_001 = "User not found";
    public static final String USER_002 = "Email already exists";
    public static final String USER_003 = "Phone number already exists";
    public static final String USER_004 = "Account number already exists";
    public static final String USER_005 = "User already activated";

    // Account Errors (ACC_XXX)
    public static final String ACC_001 = "Account not found";
    public static final String ACC_002 = "Account is inactive";
    public static final String ACC_003 = "Account is blocked";
    public static final String ACC_004 = "Account is frozen";
    public static final String ACC_005 = "Insufficient balance";
    public static final String ACC_006 = "Account already exists for this customer";

    // Customer Errors (CUST_XXX)
    public static final String CUST_001 = "Customer not found";
    public static final String CUST_002 = "Customer is suspended";
    public static final String CUST_003 = "Customer is blocked";
    public static final String CUST_004 = "KYC not completed";
    public static final String CUST_005 = "KYC verification pending";

    // Card Errors (CARD_XXX)
    public static final String CARD_001 = "Card not found";
    public static final String CARD_002 = "Card is blocked";
    public static final String CARD_003 = "Card is expired";
    public static final String CARD_004 = "Invalid card PIN";
    public static final String CARD_005 = "Card already exists for this account";
    public static final String CARD_006 = "Credit card application pending";

    // TOTP Errors (TOTP_XXX)
    public static final String TOTP_001 = "Invalid TOTP code";
    public static final String TOTP_002 = "TOTP not enabled";
    public static final String TOTP_003 = "TOTP already enabled";

    // MPIN Errors (MPIN_XXX)
    public static final String MPIN_001 = "Invalid MPIN";
    public static final String MPIN_002 = "MPIN not set";
    public static final String MPIN_003 = "MPIN already exists";

    // Device Errors (DEV_XXX)
    public static final String DEV_001 = "Device not found";
    public static final String DEV_002 = "Device not trusted";
    public static final String DEV_003 = "Maximum device limit reached";

    // Validation Errors (VAL_XXX)
    public static final String VAL_001 = "Validation failed";
    public static final String VAL_002 = "Required field missing";
    public static final String VAL_003 = "Invalid file format";
    public static final String VAL_004 = "File size exceeds limit";

    // System Errors (SYS_XXX)
    public static final String SYS_001 = "Internal server error";
    public static final String SYS_002 = "Service unavailable";
    public static final String SYS_003 = "Database error";
    public static final String SYS_004 = "Email sending failed";
}