package com.dvein.banking_backend.transaction.enums;

public enum FraudFlag {
    MULTIPLE_RAPID_TRANSFERS,
    UNUSUAL_AMOUNT,
    UNUSUAL_TIME,
    SUSPICIOUS_DEVICE,
    MULTIPLE_FAILED_ATTEMPTS
}