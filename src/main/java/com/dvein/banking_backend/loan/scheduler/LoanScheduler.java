package com.dvein.banking_backend.loan.scheduler;

import com.dvein.banking_backend.loan.model.Loan;
import com.dvein.banking_backend.loan.model.LoanSchedule;
import com.dvein.banking_backend.loan.repository.LoanRepository;
import com.dvein.banking_backend.loan.repository.LoanScheduleRepository;
import com.dvein.banking_backend.loan.service.LoanNotificationService;
import com.dvein.banking_backend.loan.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanScheduler {

    private final PenaltyService penaltyService;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanRepository loanRepository;
    private final LoanNotificationService loanNotificationService;

    /**
     * Run daily at 1 AM to process overdue loans and apply penalties
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processOverdueLoans() {
        log.info("Starting scheduled overdue loan processing");
        penaltyService.processOverdueLoans();
        log.info("Completed overdue loan processing");
    }

    /**
     * Run daily at 8 AM to send EMI reminders (3 days before due date)
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendEmiReminders() {
        log.info("Starting EMI reminder processing");

        LocalDate reminderDate = LocalDate.now().plusDays(3);
        List<LoanSchedule> upcomingEmis = loanScheduleRepository.findOverdueSchedules(reminderDate);

        for (LoanSchedule schedule : upcomingEmis) {
            if (schedule.getDueDate().equals(reminderDate)) {
                Loan loan = schedule.getLoan();
                loanNotificationService.sendEmiReminderNotification(loan.getUser(), loan);
            }
        }

        log.info("Completed EMI reminder processing");
    }
}