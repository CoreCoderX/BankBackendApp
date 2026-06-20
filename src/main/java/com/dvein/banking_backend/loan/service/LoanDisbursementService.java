package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.loan.dto.request.LoanApprovalRequest;
import com.dvein.banking_backend.loan.dto.response.LoanResponse;
import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.mapper.LoanMapper;
import com.dvein.banking_backend.loan.model.Loan;
import com.dvein.banking_backend.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final LoanService loanService;
    private final LoanMapper loanMapper;
    private final LoanNotificationService loanNotificationService;

    @Transactional
    public ApiResponse<LoanResponse> approveOrRejectLoan(LoanApprovalRequest request) {
        String adminEmail = SecurityContextHelper.getCurrentUsername();

        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new InvalidRequestException("Loan cannot be processed in current status: " + loan.getStatus());
        }

        if (request.getApproved()) {
            return approveLoan(loan, request, adminEmail);
        } else {
            return rejectLoan(loan, request, adminEmail);
        }
    }

    private ApiResponse<LoanResponse> approveLoan(Loan loan, LoanApprovalRequest request, String adminEmail) {
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedDate(LocalDate.now());
        loan.setApprovedBy(adminEmail);
        loan.setRemarks(request.getRemarks());

        Loan savedLoan = loanRepository.save(loan);

        log.info("Loan approved: {}", savedLoan.getLoanNumber());

        // Auto disburse after approval
        disburseLoan(savedLoan);

        // Send notification
        loanNotificationService.sendLoanApprovalNotification(loan.getUser(), savedLoan);

        return ApiResponse.success(
                "Loan approved and disbursed successfully",
                loanMapper.toResponse(savedLoan)
        );
    }

    private ApiResponse<LoanResponse> rejectLoan(Loan loan, LoanApprovalRequest request, String adminEmail) {
        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(request.getRejectionReason());
        loan.setApprovedBy(adminEmail);

        Loan savedLoan = loanRepository.save(loan);

        log.info("Loan rejected: {}", savedLoan.getLoanNumber());

        // Send notification
        loanNotificationService.sendLoanRejectionNotification(loan.getUser(), savedLoan);

        return ApiResponse.success(
                "Loan rejected",
                loanMapper.toResponse(savedLoan)
        );
    }

    @Transactional
    public void disburseLoan(Loan loan) {
        Account account = loan.getAccount();

        // Credit loan amount to account
        BigDecimal newBalance = account.getBalance().add(loan.getPrincipalAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Update loan status
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursedDate(LocalDate.now());
        loanRepository.save(loan);

        // Generate EMI schedule
        loanService.generateLoanSchedule(loan);

        // Activate loan
        loan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loan);

        log.info("Loan disbursed: {} - Amount: ₹{}", loan.getLoanNumber(), loan.getPrincipalAmount());

        // Send disbursement notification
        loanNotificationService.sendLoanDisbursementNotification(loan.getUser(), loan);
    }
}