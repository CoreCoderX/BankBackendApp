package com.dvein.banking_backend.common.constant;

import java.math.BigDecimal;

public class TransactionConstants {

    // Transaction Types
    public static final String INTERNAL_TRANSFER = "INTERNAL_TRANSFER";
    public static final String EXTERNAL_TRANSFER = "EXTERNAL_TRANSFER";
    public static final String UPI_TRANSFER = "UPI_TRANSFER";
    public static final String BILL_PAYMENT = "BILL_PAYMENT";
    public static final String MERCHANT_PAYMENT = "MERCHANT_PAYMENT";

    // Transaction Limits
    public static final BigDecimal DEFAULT_PER_TRANSACTION_LIMIT = BigDecimal.valueOf(50000);
    public static final BigDecimal DEFAULT_DAILY_UPI_LIMIT = BigDecimal.valueOf(100000);
    public static final BigDecimal DEFAULT_DAILY_IMPS_LIMIT = BigDecimal.valueOf(200000);
    public static final BigDecimal DEFAULT_DAILY_NEFT_LIMIT = BigDecimal.valueOf(1000000);
    public static final BigDecimal DEFAULT_DAILY_RTGS_LIMIT = BigDecimal.valueOf(5000000);
    public static final BigDecimal DEFAULT_MONTHLY_LIMIT = BigDecimal.valueOf(10000000);

    // IMPS Limits
    public static final BigDecimal IMPS_MIN_AMOUNT = BigDecimal.ONE;
    public static final BigDecimal IMPS_MAX_AMOUNT = BigDecimal.valueOf(200000);

    // NEFT Limits
    public static final BigDecimal NEFT_MIN_AMOUNT = BigDecimal.ONE;
    public static final BigDecimal NEFT_MAX_AMOUNT = BigDecimal.valueOf(10000000);

    // RTGS Limits
    public static final BigDecimal RTGS_MIN_AMOUNT = BigDecimal.valueOf(200000);

    // UPI Limits
    public static final BigDecimal UPI_MIN_AMOUNT = BigDecimal.ONE;
    public static final BigDecimal UPI_MAX_AMOUNT = BigDecimal.valueOf(100000);

    // Transaction Fees
    public static final BigDecimal IMPS_BASE_FEE = BigDecimal.valueOf(5.00);
    public static final BigDecimal NEFT_BASE_FEE = BigDecimal.ZERO;
    public static final BigDecimal RTGS_BASE_FEE = BigDecimal.valueOf(30.00);
    public static final BigDecimal UPI_BASE_FEE = BigDecimal.ZERO;
    public static final BigDecimal GST_PERCENTAGE = BigDecimal.valueOf(18.00);

    // Fraud Detection
    public static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(100000);
    public static final int MAX_TRANSACTIONS_PER_MINUTE = 5;
    public static final int SUSPICIOUS_HOUR_START = 0;
    public static final int SUSPICIOUS_HOUR_END = 5;

    // UPI
    public static final String UPI_BANK_SUFFIX = "@dveinbank";
    public static final int UPI_PIN_LENGTH = 6;
    public static final int MAX_UPI_PIN_ATTEMPTS = 3;
    public static final long UPI_PIN_LOCK_DURATION = 1800000; // 30 minutes

    // QR Code
    public static final int QR_CODE_DEFAULT_EXPIRY_HOURS = 24;
    public static final int QR_CODE_MAX_SCANS_DEFAULT = 100;

    // Scheduled Payments
    public static final int SCHEDULED_PAYMENT_MAX_RETRIES = 3;

    // External Transfer Simulation
    public static final long EXTERNAL_TRANSFER_DELAY_MS = 30000; // 30 seconds

    private TransactionConstants() {
        // Private constructor to prevent instantiation
    }
}