package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.enums.*;
import com.dvein.banking_backend.transaction.model.StandingInstruction;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.StandingInstructionRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.service.TransactionExecutionService;
import com.dvein.banking_backend.transaction.service.TransactionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StandingInstructionExecutor {

    private final StandingInstructionRepository siRepository;
    private final TransactionExecutionService executionService;
    private final TransactionNotificationService notificationService;
    private final TransactionRepository transactionRepository;

    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    @Transactional
    public void executeStandingInstructions() {
        log.info("Starting standing instruction execution job");

        LocalDate today = LocalDate.now();
        List<StandingInstruction> dueInstructions = siRepository.findDueInstructions(today);

        log.info("Found {} standing instructions due for execution", dueInstructions.size());

        for (StandingInstruction si : dueInstructions) {
            try {
                executeInstruction(si);
            } catch (Exception e) {
                log.error("Error executing standing instruction: {}", si.getId(), e);
                handleExecutionFailure(si, e.getMessage());
            }
        }
    }

    private void executeInstruction(StandingInstruction si) {
        log.info("Executing standing instruction: {}", si.getId());

        Account senderAccount = si.getSenderAccount();
        BigDecimal amount = si.getMaxAmount();

        // Check balance
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            handleExecutionFailure(si, "Insufficient balance");
            return;
        }

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId("SI-" + System.currentTimeMillis() + "-" + si.getId())
                .idempotencyKey("SI-" + si.getId() + "-" + LocalDate.now())
                .senderAccount(senderAccount)
                .receiverAccount(si.getReceiverAccount())
                .receiverAccountNumber(si.getReceiverAccountNumber())
                .receiverIfscCode(si.getReceiverIfscCode())
                .receiverName(si.getReceiverName())
                .amount(amount)
                .transactionType(si.getTransactionType())
                .transactionMode(com.dvein.banking_backend.transaction.enums.TransactionMode.STANDING_INSTRUCTION)
                .paymentMethod(si.getPaymentMethod())
                .status(TransactionStatus.INITIATED)
                .description(si.getDescription() != null ? si.getDescription() : "Standing Instruction - " + si.getId())
                .transactionFee(BigDecimal.ZERO)
                .gst(BigDecimal.ZERO)
                .totalAmount(amount)
                .ipAddress("SYSTEM")
                .build();

        transactionRepository.save(transaction);

        // Execute
        executionService.executeTransaction(transaction);

        // Update SI tracking
        si.setTotalExecutions(si.getTotalExecutions() + 1);
        si.setSuccessfulExecutions(si.getSuccessfulExecutions() + 1);
        si.setLastExecutedAt(LocalDateTime.now());
        si.setLastExecutionStatus(ExecutionStatus.COMPLETED);

        // Calculate next execution date
        LocalDate nextDate = calculateNextExecutionDate(si.getNextExecutionDate(), si.getFrequency());
        si.setNextExecutionDate(nextDate);

        // Deactivate if past end date
        if (si.getEndDate() != null && nextDate.isAfter(si.getEndDate())) {
            si.setActive(false);
        }

        siRepository.save(si);

        // Send notification
        notificationService.sendTransactionNotification(transaction);

        log.info("Standing instruction executed successfully: {}", si.getId());
    }

    private void handleExecutionFailure(StandingInstruction si, String reason) {
        si.setTotalExecutions(si.getTotalExecutions() + 1);
        si.setFailedExecutions(si.getFailedExecutions() + 1);
        si.setLastExecutedAt(LocalDateTime.now());
        si.setLastFailureReason(reason);

        siRepository.save(si);

        log.error("Standing instruction execution failed: {} - Reason: {}", si.getId(), reason);
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
            default:
                return currentDate;
        }
    }
}