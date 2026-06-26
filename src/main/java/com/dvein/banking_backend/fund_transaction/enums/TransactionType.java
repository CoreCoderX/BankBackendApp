package com.dvein.banking_backend.transaction.enums;

public enum TransactionType {
    SELF_TRANSFER,
    ACCOUNT_TRANSFER,
    BENEFICIARY_TRANSFER,
    LOAN_DISBURSEMENT,
    LOAN_REPAYMENT,
    SCHEDULED_PAYMENT,
    REVERSAL
}