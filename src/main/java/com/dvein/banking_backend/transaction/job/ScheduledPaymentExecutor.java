package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.transaction.enums.ExecutionStatus;
import com.dvein.banking_backend.transaction.enums.ScheduleFrequency;
import com.dvein.banking_backend.transaction.model.ScheduledPayment;
import com.dvein.banking_backend.transaction.repository.ScheduledPaymentRepository;
import com.dvein.banking_backend.transaction.service.InternalTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledPaymentExecutor {

    private final ScheduledPaymentRepository scheduledPaymentRepository;

    @Scheduled(cron = "0 */5 * * * ?") // Every 5 minutes
    public void executeScheduledPayments() {
        log.debug("Starting scheduled payment execution job");

        LocalDate today = LocalDate.now();
        List<ScheduledPayment> duePayments = scheduledPaymentRepository.findDuePayments(today);

        log.info("Found {} scheduled payments due for execution", duePayments.size());

        for (ScheduledPayment payment : duePayments) {
            try {
                executePayment(payment);
            } catch (Exception e) {
                log.error("Error executing scheduled payment: {}", payment.getId(), e);
                handleExecutionFailure(payment, e.getMessage());
            }
        }
    }

    private void executePayment(ScheduledPayment payment) {
        log.info("Executing scheduled payment: {}", payment.getId());

        // Here you would call the appropriate service to execute the transaction
        // For now, we'll just update the execution tracking

        payment.setTotalExecutions(payment.getTotalExecutions() + 1);
        payment.setSuccessfulExecutions(payment.getSuccessfulExecutions() + 1);
        payment.setLastExecutedAt(java.time.LocalDateTime.now());
        payment.setLastExecutionStatus(ExecutionStatus.COMPLETED);

        // Calculate next execution date
        LocalDate nextDate = calculateNextExecutionDate(payment.getNextExecutionDate(), payment.getFrequency());
        payment.setNextExecutionDate(nextDate);

        // Check if should deactivate
        if (payment.getEndDate() != null && nextDate.isAfter(payment.getEndDate())) {
            payment.setActive(false);
        }

        scheduledPaymentRepository.save(payment);
        log.info("Scheduled payment executed successfully: {}", payment.getId());
    }

    private void handleExecutionFailure(ScheduledPayment payment, String reason) {
        payment.setTotalExecutions(payment.getTotalExecutions() + 1);
        payment.setFailedExecutions(payment.getFailedExecutions() + 1);
        payment.setLastExecutedAt(java.time.LocalDateTime.now());
        payment.setLastExecutionStatus(ExecutionStatus.FAILED);
        payment.setLastFailureReason(reason);

        scheduledPaymentRepository.save(payment);
        log.error("Scheduled payment execution failed: {} - Reason: {}", payment.getId(), reason);
    }

    private LocalDate calculateNextExecutionDate(LocalDate currentDate, ScheduleFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case YEARLY:
                return currentDate.plusYears(1);
            case ONE_TIME:
            default:
                return currentDate;
        }
    }
}