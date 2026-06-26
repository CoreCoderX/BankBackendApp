package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.transaction.exception.FraudDetectedException;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    private static final int MAX_TRANSACTIONS_PER_MINUTE = 5;
    private static final BigDecimal UNUSUAL_AMOUNT_THRESHOLD = new BigDecimal("100000");

    public void checkFraudRisk(Long userId, BigDecimal amount) {
        // Check rapid transactions
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        Long recentTransactionCount = transactionRepository.countRecentSuccessfulTransactions(userId, oneMinuteAgo);

        if (recentTransactionCount >= MAX_TRANSACTIONS_PER_MINUTE) {
            log.warn("Fraud detected: Too many rapid transactions for user {}", userId);
            throw new FraudDetectedException("Too many transactions in a short period. Please try again later.");
        }

        // Check unusual amount
        if (amount.compareTo(UNUSUAL_AMOUNT_THRESHOLD) > 0) {
            log.warn("Fraud detected: Unusual transaction amount {} for user {}", amount, userId);
            // In production, this might flag for manual review instead of blocking
        }

        // Additional fraud checks can be added here
        // - Unusual time (e.g., 3 AM)
        // - New device/IP
        // - Geo-location check
        // - Velocity checks
    }
}