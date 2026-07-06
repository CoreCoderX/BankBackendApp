package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.transaction.model.DailyReconciliation;
import com.dvein.banking_backend.transaction.repository.DailyReconciliationRepository;
import com.dvein.banking_backend.transaction.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationJob {

    private final ReconciliationService reconciliationService;
    private final DailyReconciliationRepository reconciliationRepository;

    @Scheduled(cron = "0 0 1 * * ?") // 1 AM daily
    public void runDailyReconciliation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Skip if already reconciled
        if (reconciliationRepository.existsByReconciliationDate(yesterday)) {
            log.info("Reconciliation already run for date: {}", yesterday);
            return;
        }

        log.info("Starting daily reconciliation for date: {}", yesterday);

        try {
            DailyReconciliation result = reconciliationService.runReconciliation(yesterday, null);

            if (result.isBalanced()) {
                log.info("Reconciliation PASSED for date: {} - Total Txns: {}",
                        yesterday, result.getTotalTransactions());
            } else {
                log.warn("Reconciliation FAILED for date: {} - Discrepancy: {}",
                        yesterday, result.getDiscrepancy());
            }

        } catch (Exception e) {
            log.error("Reconciliation job failed for date: {}", yesterday, e);
        }
    }
}