package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.transaction.model.TransactionLimit;
import com.dvein.banking_backend.transaction.repository.TransactionLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLimitResetJob {

    private final TransactionLimitRepository limitRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Midnight every day
    @Transactional
    public void resetDailyLimits() {
        log.info("Starting daily transaction limit reset job");

        List<TransactionLimit> allLimits = limitRepository.findAll();
        LocalDate today = LocalDate.now();

        int resetCount = 0;

        for (TransactionLimit limit : allLimits) {
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
                resetCount++;
            }
        }

        log.info("Daily limit reset completed. Reset {} transaction limits", resetCount);
    }
}