package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.enums.FraudRiskLevel;
import com.dvein.banking_backend.transaction.exception.FraudDetectedException;
import com.dvein.banking_backend.transaction.model.FraudDetectionLog;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.FraudDetectionLogRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionLogRepository fraudLogRepository;

    private static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(100000);
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 5;
    private static final int SUSPICIOUS_HOUR_START = 0;
    private static final int SUSPICIOUS_HOUR_END = 5;

    @Transactional
    public FraudCheckResult checkFraud(Transaction transaction, Customer customer) {
        BigDecimal riskScore = BigDecimal.ZERO;
        StringBuilder details = new StringBuilder();
        String ruleTriggered = "NONE";

        // Rule 1: Rapid transactions
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long recentTransactions = transactionRepository.countRecentTransactionsByCustomer(
                customer.getId(), fiveMinutesAgo, LocalDateTime.now());

        if (recentTransactions >= MAX_TRANSACTIONS_PER_MINUTE) {
            riskScore = riskScore.add(BigDecimal.valueOf(30));
            ruleTriggered = "RAPID_TRANSACTIONS";
            details.append("Multiple transactions in short time. ");
        }

        // Rule 2: High-value transaction
        if (transaction.getAmount().compareTo(HIGH_VALUE_THRESHOLD) >= 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(25));
            if (ruleTriggered.equals("NONE")) {
                ruleTriggered = "HIGH_VALUE_TRANSACTION";
            }
            details.append("High-value transaction. ");
        }

        // Rule 3: Suspicious time
        LocalTime now = LocalTime.now();
        if (now.getHour() >= SUSPICIOUS_HOUR_START && now.getHour() < SUSPICIOUS_HOUR_END) {
            riskScore = riskScore.add(BigDecimal.valueOf(20));
            if (ruleTriggered.equals("NONE")) {
                ruleTriggered = "SUSPICIOUS_TIME";
            }
            details.append("Transaction during suspicious hours. ");
        }

        // Rule 4: New device
        if (transaction.getDeviceId() != null && !transaction.getDeviceId().isEmpty()) {
            // In production, check if device is new
            // For now, skip this check
        }

        // Rule 5: Duplicate transaction check
        // Already handled by idempotency key in main service

        // Determine risk level
        FraudRiskLevel riskLevel = determineRiskLevel(riskScore);

        // Log fraud detection
        if (riskScore.compareTo(BigDecimal.ZERO) > 0) {
            logFraudDetection(transaction, customer, ruleTriggered, riskScore, riskLevel, details.toString());
        }

        return FraudCheckResult.builder()
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .ruleTriggered(ruleTriggered)
                .details(details.toString())
                .shouldBlock(riskLevel == FraudRiskLevel.CRITICAL)
                .requiresOtp(riskLevel == FraudRiskLevel.MEDIUM)
                .requiresApproval(riskLevel == FraudRiskLevel.HIGH)
                .build();
    }

    private FraudRiskLevel determineRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(BigDecimal.valueOf(85)) > 0) {
            return FraudRiskLevel.CRITICAL;
        } else if (riskScore.compareTo(BigDecimal.valueOf(60)) > 0) {
            return FraudRiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(30)) > 0) {
            return FraudRiskLevel.MEDIUM;
        } else {
            return FraudRiskLevel.LOW;
        }
    }

    @Transactional
    public void logFraudDetection(Transaction transaction, Customer customer, String ruleTriggered,
                                  BigDecimal riskScore, FraudRiskLevel riskLevel, String details) {
        String actionTaken = "NONE";

        if (riskLevel == FraudRiskLevel.CRITICAL) {
            actionTaken = "TRANSACTION_BLOCKED";
        } else if (riskLevel == FraudRiskLevel.HIGH) {
            actionTaken = "REQUIRES_APPROVAL";
        } else if (riskLevel == FraudRiskLevel.MEDIUM) {
            actionTaken = "OTP_REQUIRED";
        }

        FraudDetectionLog fraudLog = FraudDetectionLog.builder()
                .transaction(transaction)
                .customer(customer)
                .ruleTriggered(ruleTriggered)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .details(details)
                .actionTaken(actionTaken)
                .build();

        fraudLogRepository.save(fraudLog);

        log.warn("Fraud detected - Customer: {}, Transaction: {}, Risk Level: {}, Score: {}",
                customer.getId(), transaction.getTransactionId(), riskLevel, riskScore);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudCheckResult {
        private BigDecimal riskScore;
        private FraudRiskLevel riskLevel;
        private String ruleTriggered;
        private String details;
        private boolean shouldBlock;
        private boolean requiresOtp;
        private boolean requiresApproval;
    }
}