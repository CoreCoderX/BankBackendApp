package com.dvein.banking_backend.transaction.enums;

public enum TransactionStatus {
    INITIATED,
    VALIDATED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED,
    REFUNDED,
    DISPUTED
}