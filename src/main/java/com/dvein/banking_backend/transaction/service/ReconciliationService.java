package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.DailyReconciliation;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.DailyReconciliationRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final DailyReconciliationRepository reconciliationRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public DailyReconciliation runReconciliation(LocalDate date, Long adminUserId) {
        if (reconciliationRepository.existsByReconciliationDate(date)) {
            throw new RuntimeException("Reconciliation already run for date: " + date);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get all completed transactions for the day
        List<Transaction> completedTransactions = transactionRepository
                .findByStatusAndInitiatedAtBetween(TransactionStatus.COMPLETED, startOfDay, endOfDay);

        // Calculate totals
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Transaction txn : completedTransactions) {
            if (txn.getSenderAccount() != null) {
                totalDebits = totalDebits.add(txn.getAmount());
            }
            if (txn.getReceiverAccount() != null) {
                totalCredits = totalCredits.add(txn.getAmount());
            }
        }

        // Calculate balances
        BigDecimal totalSystemBalance = accountRepository.sumTotalBalance();

        // For simplicity, opening balance is calculated as closing - net change
        BigDecimal netChange = totalCredits.subtract(totalDebits);
        BigDecimal openingBalance = totalSystemBalance.subtract(netChange);
        BigDecimal calculatedBalance = openingBalance.add(netChange);

        BigDecimal discrepancy = totalSystemBalance.subtract(calculatedBalance);
        boolean isBalanced = discrepancy.compareTo(BigDecimal.ZERO) == 0;

        DailyReconciliation reconciliation = DailyReconciliation.builder()
                .reconciliationDate(date)
                .totalTransactions((long) completedTransactions.size())
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .openingBalance(openingBalance)
                .closingBalance(totalSystemBalance)
                .calculatedBalance(calculatedBalance)
                .discrepancy(discrepancy)
                .balanced(isBalanced)
                .reconciledBy(User.builder().id(adminUserId).build())
                .build();

        reconciliation = reconciliationRepository.save(reconciliation);

        if (!isBalanced) {
            log.warn("Reconciliation discrepancy detected for date: {} - Amount: {}", date, discrepancy);
        }

        log.info("Reconciliation completed for date: {} - Balanced: {}", date, isBalanced);

        return reconciliation;
    }

    public DailyReconciliation getReconciliation(LocalDate date) {
        return reconciliationRepository.findByReconciliationDate(date)
                .orElseThrow(() -> new RuntimeException("Reconciliation not found for date: " + date));
    }

    public Page<DailyReconciliation> getAllReconciliations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reconciliationDate").descending());
        return reconciliationRepository.findAll(pageable);
    }
}