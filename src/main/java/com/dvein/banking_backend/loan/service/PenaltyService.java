package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import com.dvein.banking_backend.loan.model.Loan;
import com.dvein.banking_backend.loan.model.LoanPenalty;
import com.dvein.banking_backend.loan.model.LoanSchedule;
import com.dvein.banking_backend.loan.repository.LoanPenaltyRepository;
import com.dvein.banking_backend.loan.repository.LoanRepository;
import com.dvein.banking_backend.loan.repository.LoanScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {

    private final LoanPenaltyRepository loanPenaltyRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanRepository loanRepository;

    private static final BigDecimal PENALTY_PER_DAY = new BigDecimal("50");
    private static final BigDecimal LATE_FEE = new BigDecimal("500");

    @Transactional
    public void processOverdueLoans() {
        log.info("Processing overdue loans for penalty calculation");

        LocalDate today = LocalDate.now();
        List<LoanSchedule> overdueSchedules = loanScheduleRepository.findOverdueSchedules(today);

        log.info("Found {} overdue EMI schedules", overdueSchedules.size());

        for (LoanSchedule schedule : overdueSchedules) {
            applyPenalty(schedule, today);
        }
    }

    @Transactional
    public void applyPenalty(LoanSchedule schedule, LocalDate today) {
        Loan loan = schedule.getLoan();

        long daysOverdue = ChronoUnit.DAYS.between(schedule.getDueDate(), today);

        if (daysOverdue <= 0) {
            return;
        }

        // Update schedule status
        schedule.setStatus(RepaymentStatus.OVERDUE);
        loanScheduleRepository.save(schedule);

        // Update loan status
        if (loan.getStatus() == LoanStatus.ACTIVE) {
            loan.setStatus(LoanStatus.OVERDUE);
            loanRepository.save(loan);
        }

        // Calculate penalty
        BigDecimal dailyPenalty = PENALTY_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));
        BigDecimal totalPenalty = LATE_FEE.add(dailyPenalty);

        // Create penalty record
        LoanPenalty penalty = LoanPenalty.builder()
                .loan(loan)
                .amount(totalPenalty)
                .reason("EMI #" + schedule.getEmiNumber() + " overdue by " + daysOverdue + " days")
                .isPaid(false)
                .build();

        loanPenaltyRepository.save(penalty);

        log.info("Penalty applied to loan {}: ₹{}", loan.getLoanNumber(), totalPenalty);

        // Mark as NPA if overdue > 90 days
        if (daysOverdue > 90) {
            loan.setStatus(LoanStatus.NPA);
            loanRepository.save(loan);
            log.warn("Loan marked as NPA: {}", loan.getLoanNumber());
        }
    }
}