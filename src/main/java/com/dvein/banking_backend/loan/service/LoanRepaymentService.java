package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.loan.dto.request.LoanRepaymentRequest;
import com.dvein.banking_backend.loan.dto.response.LoanRepaymentResponse;
import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import com.dvein.banking_backend.loan.mapper.LoanRepaymentMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanRepaymentService {

    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanRepository loanRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final AccountRepository accountRepository;
    private final LoanValidator loanValidator;
    private final LoanRepaymentMapper loanRepaymentMapper;
    private final LoanNotificationService loanNotificationService;

    @Transactional
    public ApiResponse<LoanRepaymentResponse> makeRepayment(LoanRepaymentRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findByIdAndUserId(request.getLoanId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidRequestException("Loan is not in a repayable state: " + loan.getStatus());
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        loanValidator.validateAccountOwnership(account, userId);

        // Validate sufficient balance
        if (account.getBalance().compareTo(request.getPaymentAmount()) < 0) {
            throw new InvalidRequestException("Insufficient balance for repayment");
        }

        // Get next pending EMI
        LoanSchedule nextEmi = loanScheduleRepository.findNextPendingEmi(loan.getId())
                .orElseThrow(() -> new InvalidRequestException("No pending EMI found"));

        // Calculate penalty if any
        BigDecimal penaltyDue = loanPenaltyRepository.getUnpaidPenaltyForLoan(loan.getId());
        if (penaltyDue == null) {
            penaltyDue = BigDecimal.ZERO;
        }

        // Process payment allocation
        BigDecimal paymentAmount = request.getPaymentAmount();
        BigDecimal interestComponent = nextEmi.getInterestComponent();
        BigDecimal principalComponent = nextEmi.getPrincipalComponent();

        // Debit account
        BigDecimal newAccountBalance = account.getBalance().subtract(paymentAmount);
        account.setBalance(newAccountBalance);
        accountRepository.save(account);

        // Update remaining principal
        BigDecimal newRemainingPrincipal = loan.getRemainingPrincipal().subtract(principalComponent);
        if (newRemainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            newRemainingPrincipal = BigDecimal.ZERO;
        }
        loan.setRemainingPrincipal(newRemainingPrincipal);
        loan.setAmountPaid(loan.getAmountPaid().add(paymentAmount));

        // Mark EMI as paid
        nextEmi.setStatus(RepaymentStatus.PAID);
        nextEmi.setPaidDate(LocalDate.now());
        loanScheduleRepository.save(nextEmi);

        // Mark penalties as paid
        if (penaltyDue.compareTo(BigDecimal.ZERO) > 0) {
            loanPenaltyRepository.findByLoanIdAndIsPaidFalse(loan.getId())
                    .forEach(penalty -> {
                        penalty.setIsPaid(true);
                        loanPenaltyRepository.save(penalty);
                    });
        }

        // Create repayment record
        String transactionId = "TXN" + System.currentTimeMillis();

        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .paymentAmount(paymentAmount)
                .principalPaid(principalComponent)
                .interestPaid(interestComponent)
                .penaltyPaid(penaltyDue)
                .remainingBalance(newRemainingPrincipal)
                .paymentDate(LocalDate.now())
                .status(RepaymentStatus.PAID)
                .transactionId(transactionId)
                .remarks(request.getRemarks())
                .build();

        LoanRepayment savedRepayment = loanRepaymentRepository.save(repayment);

        // Check if loan is fully paid
        Long pendingEmis = loanScheduleRepository.countPendingEmis(loan.getId());
        if (pendingEmis == 0 || newRemainingPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedDate(LocalDate.now());
            loanNotificationService.sendLoanClosureNotification(loan.getUser(), loan);
        } else {
            loan.setStatus(LoanStatus.ACTIVE);
        }

        loanRepository.save(loan);

        log.info("Loan repayment processed: {} - Amount: ₹{}", loan.getLoanNumber(), paymentAmount);

        // Send notification
        loanNotificationService.sendEmiPaidNotification(loan.getUser(), loan, savedRepayment);

        return ApiResponse.success(
                "Repayment processed successfully",
                loanRepaymentMapper.toResponse(savedRepayment)
        );
    }

    public ApiResponse<PageResponse<LoanRepaymentResponse>> getRepaymentHistory(
            Long loanId, int page, int size
    ) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanRepayment> repaymentPage =
                loanRepaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId, pageable);

        List<LoanRepaymentResponse> responses = repaymentPage.getContent().stream()
                .map(loanRepaymentMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<LoanRepaymentResponse> pageResponse = PageResponse.<LoanRepaymentResponse>builder()
                .content(responses)
                .pageNumber(repaymentPage.getNumber())
                .pageSize(repaymentPage.getSize())
                .totalElements(repaymentPage.getTotalElements())
                .totalPages(repaymentPage.getTotalPages())
                .last(repaymentPage.isLast())
                .build();

        return ApiResponse.success("Repayment history retrieved successfully", pageResponse);
    }
}