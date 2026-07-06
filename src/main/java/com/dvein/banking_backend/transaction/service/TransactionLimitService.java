package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.UpdateTransactionLimitRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionLimitResponse;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.exception.TransactionLimitExceededException;
import com.dvein.banking_backend.transaction.model.TransactionLimit;
import com.dvein.banking_backend.transaction.repository.TransactionLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLimitService {

    private final TransactionLimitRepository limitRepository;
    private final CustomerRepository customerRepository;
    private final SecurityContextHelper securityContextHelper;

    @Transactional
    public TransactionLimit getOrCreateLimit(Customer customer) {
        return limitRepository.findByCustomer(customer)
                .orElseGet(() -> createDefaultLimit(customer));
    }

    @Transactional
    public TransactionLimit createDefaultLimit(Customer customer) {
        TransactionLimit limit = TransactionLimit.builder()
                .customer(customer)
                .perTransactionLimit(BigDecimal.valueOf(50000))
                .dailyUpiLimit(BigDecimal.valueOf(100000))
                .dailyImpsLimit(BigDecimal.valueOf(200000))
                .dailyNeftLimit(BigDecimal.valueOf(1000000))
                .dailyRtgsLimit(BigDecimal.valueOf(5000000))
                .dailyQrLimit(BigDecimal.valueOf(100000))
                .monthlyTransferLimit(BigDecimal.valueOf(10000000))
                .build();

        return limitRepository.save(limit);
    }

    @Transactional
    public void validateAndUpdateLimit(TransactionLimit limit, TransactionType type, BigDecimal amount) {
        resetLimitIfNeeded(limit);

        // Validate per transaction limit
        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw new TransactionLimitExceededException(
                    String.format("Per transaction limit exceeded. Limit: %.2f", limit.getPerTransactionLimit()));
        }

        // Validate daily limit based on type
        switch (type) {
            case UPI_TRANSFER:
                if (limit.getDailyUpiUsed().add(amount).compareTo(limit.getDailyUpiLimit()) > 0) {
                    throw new TransactionLimitExceededException("Daily UPI limit exceeded");
                }
                limit.setDailyUpiUsed(limit.getDailyUpiUsed().add(amount));
                break;

            case IMPS:
                if (limit.getDailyImpsUsed().add(amount).compareTo(limit.getDailyImpsLimit()) > 0) {
                    throw new TransactionLimitExceededException("Daily IMPS limit exceeded");
                }
                limit.setDailyImpsUsed(limit.getDailyImpsUsed().add(amount));
                break;

            case NEFT:
                if (limit.getDailyNeftUsed().add(amount).compareTo(limit.getDailyNeftLimit()) > 0) {
                    throw new TransactionLimitExceededException("Daily NEFT limit exceeded");
                }
                limit.setDailyNeftUsed(limit.getDailyNeftUsed().add(amount));
                break;

            case RTGS:
                if (limit.getDailyRtgsUsed().add(amount).compareTo(limit.getDailyRtgsLimit()) > 0) {
                    throw new TransactionLimitExceededException("Daily RTGS limit exceeded");
                }
                limit.setDailyRtgsUsed(limit.getDailyRtgsUsed().add(amount));
                break;

            default:
                break;
        }

        // Validate monthly limit
        if (limit.getMonthlyUsed().add(amount).compareTo(limit.getMonthlyTransferLimit()) > 0) {
            throw new TransactionLimitExceededException("Monthly transfer limit exceeded");
        }

        limit.setMonthlyUsed(limit.getMonthlyUsed().add(amount));
        limitRepository.save(limit);
    }

    @Transactional
    public void resetLimitIfNeeded(TransactionLimit limit) {
        LocalDate today = LocalDate.now();

        if (limit.getLastResetDate() == null || limit.getLastResetDate().isBefore(today)) {
            limit.setDailyUpiUsed(BigDecimal.ZERO);
            limit.setDailyImpsUsed(BigDecimal.ZERO);
            limit.setDailyNeftUsed(BigDecimal.ZERO);
            limit.setDailyRtgsUsed(BigDecimal.ZERO);
            limit.setDailyQrUsed(BigDecimal.ZERO);
            limit.setLastResetDate(today);

            // Reset monthly on 1st of month
            if (today.getDayOfMonth() == 1) {
                limit.setMonthlyUsed(BigDecimal.ZERO);
            }

            limitRepository.save(limit);
            log.info("Transaction limits reset for customer: {}", limit.getCustomer().getId());
        }
    }

    public TransactionLimitResponse getLimits(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        TransactionLimit limit = getOrCreateLimit(customer);
        resetLimitIfNeeded(limit);

        return mapToResponse(limit);
    }

    @Transactional
    public TransactionLimitResponse updateLimits(UpdateTransactionLimitRequest request, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        TransactionLimit limit = getOrCreateLimit(customer);

        if (request.getPerTransactionLimit() != null) {
            limit.setPerTransactionLimit(request.getPerTransactionLimit());
        }
        if (request.getDailyUpiLimit() != null) {
            limit.setDailyUpiLimit(request.getDailyUpiLimit());
        }
        if (request.getDailyImpsLimit() != null) {
            limit.setDailyImpsLimit(request.getDailyImpsLimit());
        }
        if (request.getDailyNeftLimit() != null) {
            limit.setDailyNeftLimit(request.getDailyNeftLimit());
        }
        if (request.getDailyRtgsLimit() != null) {
            limit.setDailyRtgsLimit(request.getDailyRtgsLimit());
        }
        if (request.getDailyQrLimit() != null) {
            limit.setDailyQrLimit(request.getDailyQrLimit());
        }
        if (request.getMonthlyTransferLimit() != null) {
            limit.setMonthlyTransferLimit(request.getMonthlyTransferLimit());
        }

        limit = limitRepository.save(limit);
        log.info("Transaction limits updated for customer: {}", customer.getId());

        return mapToResponse(limit);
    }

    @Transactional
    public void resetLimits(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        TransactionLimit limit = getOrCreateLimit(customer);
        limit.setDailyUpiUsed(BigDecimal.ZERO);
        limit.setDailyImpsUsed(BigDecimal.ZERO);
        limit.setDailyNeftUsed(BigDecimal.ZERO);
        limit.setDailyRtgsUsed(BigDecimal.ZERO);
        limit.setDailyQrUsed(BigDecimal.ZERO);
        limit.setMonthlyUsed(BigDecimal.ZERO);
        limit.setLastResetDate(LocalDate.now());

        limitRepository.save(limit);
        log.info("Transaction limits manually reset for customer: {}", customer.getId());
    }

    private TransactionLimitResponse mapToResponse(TransactionLimit limit) {
        return TransactionLimitResponse.builder()
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyUpiLimit(limit.getDailyUpiLimit())
                .dailyImpsLimit(limit.getDailyImpsLimit())
                .dailyNeftLimit(limit.getDailyNeftLimit())
                .dailyRtgsLimit(limit.getDailyRtgsLimit())
                .dailyQrLimit(limit.getDailyQrLimit())
                .monthlyTransferLimit(limit.getMonthlyTransferLimit())
                .dailyUpiUsed(limit.getDailyUpiUsed())
                .dailyImpsUsed(limit.getDailyImpsUsed())
                .dailyNeftUsed(limit.getDailyNeftUsed())
                .dailyRtgsUsed(limit.getDailyRtgsUsed())
                .dailyQrUsed(limit.getDailyQrUsed())
                .monthlyUsed(limit.getMonthlyUsed())
                .lastResetDate(limit.getLastResetDate())
                .dailyUpiAvailable(limit.getDailyUpiLimit().subtract(limit.getDailyUpiUsed()))
                .dailyImpsAvailable(limit.getDailyImpsLimit().subtract(limit.getDailyImpsUsed()))
                .dailyNeftAvailable(limit.getDailyNeftLimit().subtract(limit.getDailyNeftUsed()))
                .dailyRtgsAvailable(limit.getDailyRtgsLimit().subtract(limit.getDailyRtgsUsed()))
                .monthlyAvailable(limit.getMonthlyTransferLimit().subtract(limit.getMonthlyUsed()))
                .build();
    }
}