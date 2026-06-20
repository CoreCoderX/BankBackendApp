package com.dvein.banking_backend.transaction.scheduler;

import com.dvein.banking_backend.transaction.dto.request.BeneficiaryTransferRequest;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.ScheduledPayment;
import com.dvein.banking_backend.transaction.repository.ScheduledPaymentRepository;
import com.dvein.banking_backend.transaction.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentExecutor {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final FundTransferService fundTransferService;

    @Scheduled(cron = "0 0 9 * * *") // Run daily at 9 AM
    public void executeScheduledPayments() {
        log.info("Starting scheduled payment execution");

        LocalDate today = LocalDate.now();
        List<ScheduledPayment> duePayments = scheduledPaymentRepository.findDuePayments(
                today, TransactionStatus.PENDING
        );

        log.info("Found {} due scheduled payments", duePayments.size());

        for (ScheduledPayment payment : duePayments) {
            try {
                executePayment(payment);
            } catch (Exception e) {
                log.error("Failed to execute scheduled payment {}: {}", payment.getId(), e.getMessage());
            }
        }

        log.info("Completed scheduled payment execution");
    }

    private void executePayment(ScheduledPayment payment) {
        BeneficiaryTransferRequest request = BeneficiaryTransferRequest.builder()
                .senderAccountId(payment.getUser().getId())
                .beneficiaryId(payment.getBeneficiary().getId())
                .amount(payment.getAmount())
                .remarks(payment.getRemarks())
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        // Execute transfer
        fundTransferService.beneficiaryTransfer(request);

        // Update next execution date
        LocalDate nextDate = calculateNextExecutionDate(payment);
        payment.setNextExecutionDate(nextDate);
        scheduledPaymentRepository.save(payment);

        log.info("Executed scheduled payment {}", payment.getId());
    }

    private LocalDate calculateNextExecutionDate(ScheduledPayment payment) {
        LocalDate current = payment.getNextExecutionDate();

        return switch (payment.getFrequency()) {
            case "DAILY" -> current.plusDays(1);
            case "WEEKLY" -> current.plusWeeks(1);
            case "MONTHLY" -> current.plusMonths(1);
            default -> current.plusMonths(1);
        };
    }
}