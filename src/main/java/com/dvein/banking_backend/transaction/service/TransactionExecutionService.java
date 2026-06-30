package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionExecutionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void executeTransaction(Transaction transaction) {
        try {
            // Update status to PROCESSING
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setProcessingAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            Account senderAccount = transaction.getSenderAccount();
            Account receiverAccount = transaction.getReceiverAccount();

            if (senderAccount != null) {
                // Debit sender account with optimistic locking
                debitAccount(senderAccount, transaction.getTotalAmount(), transaction);
            }

            if (receiverAccount != null) {
                // Credit receiver account with optimistic locking
                creditAccount(receiverAccount, transaction.getAmount(), transaction);
            }

            // Simulate processing delay for external transfers
            if (transaction.getReceiverAccount() == null) {
                simulateExternalTransferDelay();
            }

            // Mark transaction as completed
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            log.info("Transaction executed successfully: {}", transaction.getTransactionId());

        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Concurrent transaction detected for: {}", transaction.getTransactionId());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason("Concurrent transaction conflict. Please retry.");
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction failed due to concurrent modification", e);

        } catch (Exception e) {
            log.error("Transaction execution failed: {}", transaction.getTransactionId(), e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason("Transaction processing error: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction execution failed", e);
        }
    }

    @Transactional
    public void debitAccount(Account account, BigDecimal amount, Transaction transaction) {
        // Capture balance before
        transaction.setSenderBalanceBefore(account.getBalance());

        // Debit amount
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        // Save with version check (optimistic locking)
        accountRepository.save(account);

        // Capture balance after
        transaction.setSenderBalanceAfter(newBalance);

        log.debug("Debited {} from account {}. New balance: {}",
                amount, account.getAccountNumber(), newBalance);
    }

    @Transactional
    public void creditAccount(Account account, BigDecimal amount, Transaction transaction) {
        // Capture balance before
        transaction.setReceiverBalanceBefore(account.getBalance());

        // Credit amount
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        // Save with version check (optimistic locking)
        accountRepository.save(account);

        // Capture balance after
        transaction.setReceiverBalanceAfter(newBalance);

        log.debug("Credited {} to account {}. New balance: {}",
                amount, account.getAccountNumber(), newBalance);
    }

    private void simulateExternalTransferDelay() {
        try {
            // Simulate 30 seconds processing time for external transfers
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("External transfer simulation interrupted");
        }
    }
}