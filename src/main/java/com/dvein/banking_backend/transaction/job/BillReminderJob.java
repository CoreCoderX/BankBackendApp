package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.notification.service.EmailService;
import com.dvein.banking_backend.transaction.model.Biller;
import com.dvein.banking_backend.transaction.repository.BillerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillReminderJob {

    private final BillerRepository billerRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * ?") // 8 AM daily
    public void sendBillReminders() {
        log.debug("Starting bill reminder job");

        // Get all billers with auto-pay enabled
        List<Biller> autoPayBillers = billerRepository.findByAutoPayEnabledTrue();

        int remindersSent = 0;

        for (Biller biller : autoPayBillers) {
            try {
                String email = biller.getCustomer().getUser().getEmail();
                String subject = "Bill Payment Reminder - " + biller.getBillerName();

                String message = String.format(
                        "Dear Customer,\n\n" +
                                "This is a reminder for your upcoming bill payment.\n\n" +
                                "Biller: %s\n" +
                                "Category: %s\n" +
                                "Account Number: %s\n\n" +
                                "Please ensure sufficient balance for auto-pay.\n\n" +
                                "DVein Bank Team",
                        biller.getBillerName(),
                        biller.getBillerCategory(),
                        biller.getAccountNumber()
                );

                emailService.sendOtpEmail(email, subject, message);
                remindersSent++;

            } catch (Exception e) {
                log.error("Failed to send bill reminder for biller: {}", biller.getId(), e);
            }
        }

        log.info("Bill reminder job completed - Reminders sent: {}", remindersSent);
    }
}