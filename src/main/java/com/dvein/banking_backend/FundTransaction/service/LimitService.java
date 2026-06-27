package com.dvein.banking_backend.FundTransaction.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.FundTransaction.exception.TransferLimitExceededException;
import com.dvein.banking_backend.FundTransaction.model.TransactionLimit;
import com.dvein.banking_backend.FundTransaction.repository.TransactionLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LimitService {

    private final TransactionLimitRepository limitRepository;
    private final UserRepository userRepository;

    // ✅ Default limits
    private static final BigDecimal DEFAULT_PER_TRANSACTION_LIMIT = new BigDecimal("500000"); // ₹5,00,000
    private static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("1000000"); // ₹10,00,000
    private static final BigDecimal DEFAULT_MONTHLY_LIMIT = new BigDecimal("10000000"); // ₹1,00,00,000

    @Transactional
    public void validateTransferLimit(Long userId, BigDecimal amount) {
        // ✅ Null check for amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        TransactionLimit limit = getOrCreateLimit(userId);

        // ✅ Null check for per transaction limit
        BigDecimal perTransactionLimit = limit.getPerTransactionLimit();
        if (perTransactionLimit == null) {
            perTransactionLimit = DEFAULT_PER_TRANSACTION_LIMIT;
        }

        if (amount.compareTo(perTransactionLimit) > 0) {
            throw new TransferLimitExceededException(
                    "Amount exceeds per transaction limit of ₹" + perTransactionLimit
            );
        }

        // ✅ Null check for daily limit
        BigDecimal dailyLimit = limit.getDailyLimit();
        if (dailyLimit == null) {
            dailyLimit = DEFAULT_DAILY_LIMIT;
        }

        BigDecimal dailyUsed = limit.getDailyUsed();
        if (dailyUsed == null) {
            dailyUsed = BigDecimal.ZERO;
        }

        BigDecimal newDailyUsed = dailyUsed.add(amount);
        if (newDailyUsed.compareTo(dailyLimit) > 0) {
            throw new TransferLimitExceededException(
                    "Amount exceeds daily transfer limit. Remaining: ₹" +
                            dailyLimit.subtract(dailyUsed)
            );
        }

        // ✅ Null check for monthly limit
        BigDecimal monthlyLimit = limit.getMonthlyLimit();
        if (monthlyLimit == null) {
            monthlyLimit = DEFAULT_MONTHLY_LIMIT;
        }

        BigDecimal monthlyUsed = limit.getMonthlyUsed();
        if (monthlyUsed == null) {
            monthlyUsed = BigDecimal.ZERO;
        }

        BigDecimal newMonthlyUsed = monthlyUsed.add(amount);
        if (newMonthlyUsed.compareTo(monthlyLimit) > 0) {
            throw new TransferLimitExceededException(
                    "Amount exceeds monthly transfer limit. Remaining: ₹" +
                            monthlyLimit.subtract(monthlyUsed)
            );
        }

        log.info("Transfer limit validated for user: {} amount: ₹{}", userId, amount);
    }

    @Transactional
    public void updateTransferLimit(Long userId, BigDecimal amount) {
        TransactionLimit limit = getOrCreateLimit(userId);

        BigDecimal dailyUsed = limit.getDailyUsed();
        if (dailyUsed == null) {
            dailyUsed = BigDecimal.ZERO;
        }

        BigDecimal monthlyUsed = limit.getMonthlyUsed();
        if (monthlyUsed == null) {
            monthlyUsed = BigDecimal.ZERO;
        }

        limit.setDailyUsed(dailyUsed.add(amount));
        limit.setMonthlyUsed(monthlyUsed.add(amount));

        limitRepository.save(limit);
        log.info("Updated transfer limits for user: {}", userId);
    }

    private TransactionLimit getOrCreateLimit(Long userId) {
        LocalDate today = LocalDate.now();

        TransactionLimit limit = limitRepository.findByUserIdAndCurrentDate(userId, today)
                .orElse(null);

        // If no limit for today, create or reset
        if (limit == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            limit = TransactionLimit.builder()
                    .user(user)
                    .currentDate(today)
                    .currentMonth(today.getMonthValue())
                    .currentYear(today.getYear())
                    .perTransactionLimit(DEFAULT_PER_TRANSACTION_LIMIT)  // ✅ Set default
                    .dailyLimit(DEFAULT_DAILY_LIMIT)                      // ✅ Set default
                    .monthlyLimit(DEFAULT_MONTHLY_LIMIT)                  // ✅ Set default
                    .dailyUsed(BigDecimal.ZERO)
                    .monthlyUsed(BigDecimal.ZERO)
                    .build();

            // Check if we need to carry over monthly usage
            TransactionLimit previousLimit = limitRepository.findByUserIdAndMonth(
                    userId,
                    today.getMonthValue(),
                    today.getYear()
            ).orElse(null);

            if (previousLimit != null && previousLimit.getCurrentMonth().equals(today.getMonthValue())) {
                limit.setMonthlyUsed(previousLimit.getMonthlyUsed());
            }

            limit = limitRepository.save(limit);
            log.info("Created new transaction limit for user: {} with defaults", userId);
        }

        return limit;
    }
}