package com.dvein.banking_backend.common.enums;

public enum AuthenticationState {
    // Initial states
    CREDENTIALS_VALIDATED,

    // Multi-factor states
    REQUIRES_DEVICE_VERIFICATION,
    DEVICE_VERIFIED,
    REQUIRES_TOTP,
    TOTP_VERIFIED,

    // Final state
    FULLY_AUTHENTICATED,

    // Error states
    AUTHENTICATION_FAILED,
    ACCOUNT_LOCKED,
    ACCOUNT_INACTIVE
}