package com.dvein.banking_backend.common.constant;

public class TransactionMessages {

    // Success Messages
    public static final String INTERNAL_TRANSFER_SUCCESS = "Internal transfer completed successfully";
    public static final String EXTERNAL_TRANSFER_SUCCESS = "External transfer initiated successfully";
    public static final String UPI_TRANSFER_SUCCESS = "UPI payment successful";
    public static final String BILL_PAYMENT_SUCCESS = "Bill paid successfully";
    public static final String MERCHANT_PAYMENT_SUCCESS = "Payment to merchant successful";
    public static final String QR_PAYMENT_SUCCESS = "QR payment successful";
    public static final String SCHEDULED_PAYMENT_CREATED = "Scheduled payment created successfully";
    public static final String STANDING_INSTRUCTION_CREATED = "Standing instruction created successfully";

    // Error Messages
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance in account";
    public static final String TRANSACTION_LIMIT_EXCEEDED = "Transaction limit exceeded";
    public static final String DAILY_LIMIT_EXCEEDED = "Daily transaction limit exceeded";
    public static final String MONTHLY_LIMIT_EXCEEDED = "Monthly transaction limit exceeded";
    public static final String INVALID_ACCOUNT = "Invalid account number";
    public static final String INVALID_UPI_ID = "Invalid UPI ID";
    public static final String UPI_PIN_INCORRECT = "Incorrect UPI PIN";
    public static final String UPI_PIN_LOCKED = "UPI PIN locked due to multiple failed attempts";
    public static final String DUPLICATE_TRANSACTION = "Duplicate transaction detected";
    public static final String FRAUD_DETECTED = "Transaction blocked due to suspicious activity";
    public static final String ACCOUNT_INACTIVE = "Account is not active";
    public static final String KYC_NOT_VERIFIED = "KYC verification pending";
    public static final String SAME_ACCOUNT_TRANSFER = "Cannot transfer to the same account";

    // UPI Messages
    public static final String UPI_PROFILE_CREATED = "UPI profile created successfully";
    public static final String UPI_ID_CREATED = "UPI ID created successfully";
    public static final String UPI_PIN_CREATED = "UPI PIN created successfully";
    public static final String UPI_PIN_CHANGED = "UPI PIN changed successfully";
    public static final String QR_CODE_GENERATED = "QR code generated successfully";
    public static final String COLLECT_REQUEST_SENT = "Money request sent successfully";
    public static final String COLLECT_REQUEST_APPROVED = "Money request approved";
    public static final String COLLECT_REQUEST_REJECTED = "Money request rejected";

    // Limit Messages
    public static final String LIMITS_UPDATED = "Transaction limits updated successfully";
    public static final String LIMITS_RESET = "Transaction limits reset successfully";

    private TransactionMessages() {
        // Private constructor
    }
}