package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.exception.TransferLimitExceededException;
import com.dvein.banking_backend.transaction.model.TransactionLimit;
import com.dvein.banking_backend.transaction.repository.TransactionLimitRepository;
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

    @Transactional
    public void validateTransferLimit(Long userId, BigDecimal amount) {
        // Validate per transaction limit
        TransactionLimit limit = getOrCreateLimit(userId);

        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw new TransferLimitExceededException(
                    "Amount exceeds per transaction limit of ₹" + limit.getPerTransactionLimit()
            );
        }

        // Validate daily limit
        BigDecimal newDailyUsed = limit.getDailyUsed().add(amount);
        if (newDailyUsed.compareTo(limit.getDailyLimit()) > 0) {
            throw new TransferLimitExceededException(
                    "Amount exceeds daily transfer limit. Remaining: ₹" +
                            limit.getDailyLimit().subtract(limit.getDailyUsed())
            );
        }

        // Validate monthly limit
        BigDecimal newMonthlyUsed = limit.getMonthlyUsed().add(amount);
        if (newMonthlyUsed.compareTo(limit.getMonthlyLimit()) > 0) {
            throw new TransferLimitExceededException(
                    "Amount exceeds monthly transfer limit. Remaining: ₹" +
                            limit.getMonthlyLimit().subtract(limit.getMonthlyUsed())
            );
        }
    }

    @Transactional
    public void updateTransferLimit(Long userId, BigDecimal amount) {
        TransactionLimit limit = getOrCreateLimit(userId);

        limit.setDailyUsed(limit.getDailyUsed().add(amount));
        limit.setMonthlyUsed(limit.getMonthlyUsed().add(amount));

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
        }

        return limit;
    }
}