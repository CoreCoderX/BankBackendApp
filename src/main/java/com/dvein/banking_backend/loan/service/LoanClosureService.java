package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.loan.dto.request.LoanClosureRequest;
import com.dvein.banking_backend.loan.dto.response.LoanResponse;
import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import com.dvein.banking_backend.loan.mapper.LoanMapper;
import com.dvein.banking_backend.loan.model.Loan;
import com.dvein.banking_backend.loan.model.LoanRepayment;
import com.dvein.banking_backend.loan.model.LoanSchedule;
import com.dvein.banking_backend.loan.repository.LoanPenaltyRepository;
import com.dvein.banking_backend.loan.repository.LoanRepaymentRepository;
import com.dvein.banking_backend.loan.repository.LoanRepository;
import com.dvein.banking_backend.loan.repository.LoanScheduleRepository;
import com.dvein.banking_backend.loan.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanClosureService {

    private final LoanRepository loanRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final AccountRepository accountRepository;
    private final LoanValidator loanValidator;
    private final LoanMapper loanMapper;
    private final LoanNotificationService loanNotificationService;

    private static final BigDecimal FORECLOSURE_CHARGE_PERCENT = new BigDecimal("2"); // 2%

    @Transactional
    public ApiResponse<LoanResponse> foreclosureLoan(LoanClosureRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findByIdAndUserId(request.getLoanId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidRequestException("Loan cannot be closed in current status: " + loan.getStatus());
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        loanValidator.validateAccountOwnership(account, userId);

        // Calculate foreclosure amount
        BigDecimal remainingPrincipal = loan.getRemainingPrincipal();

        // Foreclosure charges (2% of remaining principal)
        BigDecimal foreclosureCharge = remainingPrincipal
                .multiply(FORECLOSURE_CHARGE_PERCENT)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Interest till today
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal interestTillDate = remainingPrincipal.multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Penalty due
        BigDecimal penaltyDue = loanPenaltyRepository.getUnpaidPenaltyForLoan(loan.getId());
        if (penaltyDue == null) {
            penaltyDue = BigDecimal.ZERO;
        }

        BigDecimal totalForeclosureAmount = remainingPrincipal
                .add(foreclosureCharge)
                .add(interestTillDate)
                .add(penaltyDue);

        // Validate balance
        if (account.getBalance().compareTo(totalForeclosureAmount) < 0) {
            throw new InvalidRequestException(
                    "Insufficient balance. Required: ₹" + totalForeclosureAmount
            );
        }

        // Debit account
        BigDecimal newBalance = account.getBalance().subtract(totalForeclosureAmount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Close all pending EMIs
        loanScheduleRepository.findByLoanIdAndStatus(loan.getId(), RepaymentStatus.PENDING)
                .forEach(schedule -> {
                    schedule.setStatus(RepaymentStatus.PAID);
                    schedule.setPaidDate(LocalDate.now());
                    loanScheduleRepository.save(schedule);
                });

        // Mark penalties as paid
        loanPenaltyRepository.findByLoanIdAndIsPaidFalse(loan.getId())
                .forEach(penalty -> {
                    penalty.setIsPaid(true);
                    loanPenaltyRepository.save(penalty);
                });

        // Create final repayment record
        String transactionId = "TXN" + System.currentTimeMillis();

        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .paymentAmount(totalForeclosureAmount)
                .principalPaid(remainingPrincipal)
                .interestPaid(interestTillDate)
                .penaltyPaid(penaltyDue.add(foreclosureCharge))
                .remainingBalance(BigDecimal.ZERO)
                .paymentDate(LocalDate.now())
                .status(RepaymentStatus.PAID)
                .transactionId(transactionId)
                .remarks("Loan Foreclosure - " + request.getRemarks())
                .build();

        loanRepaymentRepository.save(repayment);

        // Close loan
        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedDate(LocalDate.now());
        loan.setRemainingPrincipal(BigDecimal.ZERO);
        loan.setAmountPaid(loan.getAmountPaid().add(totalForeclosureAmount));

        Loan closedLoan = loanRepository.save(loan);

        log.info("Loan foreclosed: {} - Amount: ₹{}", loan.getLoanNumber(), totalForeclosureAmount);

        // Send notification
        loanNotificationService.sendLoanClosureNotification(loan.getUser(), closedLoan);

        return ApiResponse.success(
                "Loan closed successfully. Total paid: ₹" + totalForeclosureAmount,
                loanMapper.toResponse(closedLoan)
        );
    }
}