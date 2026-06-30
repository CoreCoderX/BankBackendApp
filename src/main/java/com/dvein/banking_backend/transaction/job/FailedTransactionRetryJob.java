package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionMetadata;
import com.dvein.banking_backend.transaction.repository.TransactionMetadataRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.service.TransactionExecutionService;
import com.dvein.banking_backend.transaction.service.TransactionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedTransactionRetryJob {

    private final TransactionRepository transactionRepository;
    private final TransactionMetadataRepository metadataRepository;
    private final TransactionExecutionService executionService;
    private final TransactionNotificationService notificationService;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_WINDOW_MINUTES = 10; // Only retry transactions failed within last 10 minutes for this run

    @Scheduled(cron = "0 */10 * * * ?") // Every 10 minutes
    @Transactional
    public void retryFailedTransactions() {
        log.debug("Starting failed transaction retry job");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(RETRY_WINDOW_MINUTES);

        List<Transaction> failedTransactions = transactionRepository
                .findByStatusAndInitiatedAtBefore(TransactionStatus.FAILED, cutoffTime);

        int retriedCount = 0;
        int successCount = 0;
        int finalFailureCount = 0;

        for (Transaction transaction : failedTransactions) {
            try {
                int retryCount = getRetryCount(transaction);

                if (retryCount >= MAX_RETRY_ATTEMPTS) {
                    log.info("Transaction {} exceeded max retry attempts ({})",
                            transaction.getTransactionId(), retryCount);
                    finalFailureCount++;
                    continue;
                }

                // Increment retry count
                incrementRetryCount(transaction);

                // Reset status and retry
                transaction.setStatus(TransactionStatus.INITIATED);
                transaction.setFailureReason(null);
                transactionRepository.save(transaction);

                executionService.executeTransaction(transaction);
                retriedCount++;
                successCount++;

                log.info("Successfully retried transaction: {}", transaction.getTransactionId());

            } catch (Exception e) {
                log.error("Retry failed for transaction: {} - Error: {}",
                        transaction.getTransactionId(), e.getMessage());
                finalFailureCount++;
            }
        }

        if (retriedCount > 0) {
            log.info("Retry job completed - Attempted: {}, Success: {}, Final Failures: {}",
                    retriedCount, successCount, finalFailureCount);
        }
    }

    private int getRetryCount(Transaction transaction) {
        Optional<TransactionMetadata> retryMeta = metadataRepository
                .findByTransactionAndMetaKey(transaction, "retry_count");

        if (retryMeta.isPresent()) {
            try {
                return Integer.parseInt(retryMeta.get().getMetaValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private void incrementRetryCount(Transaction transaction) {
        Optional<TransactionMetadata> retryMeta = metadataRepository
                .findByTransactionAndMetaKey(transaction, "retry_count");

        int newCount;
        if (retryMeta.isPresent()) {
            TransactionMetadata meta = retryMeta.get();
            try {
                newCount = Integer.parseInt(meta.getMetaValue()) + 1;
            } catch (NumberFormatException e) {
                newCount = 1;
            }
            meta.setMetaValue(String.valueOf(newCount));
            metadataRepository.save(meta);
        } else {
            TransactionMetadata meta = TransactionMetadata.builder()
                    .transaction(transaction)
                    .metaKey("retry_count")
                    .metaValue("1")
                    .build();
            metadataRepository.save(meta);
        }
    }
}