package com.dvein.banking_backend.transaction.enums;

public enum TransactionStatus {
    INITIATED,
    VALIDATED,
    AUTHORIZED,
    PENDING,
    SUCCESS,
    FAILED,
    REVERSED,
    FLAGGED
}