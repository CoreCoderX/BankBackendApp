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
        try {
            loan.setStatus(LoanStatus.APPROVED);
            loan.setApprovedDate(LocalDate.now());
            loan.setApprovedBy(adminEmail);
            loan.setRemarks(request.getRemarks());

            Loan savedLoan = loanRepository.save(loan);
            log.info("Loan approved: {}", savedLoan.getLoanNumber());

            // Auto disburse after approval
            disburseLoan(savedLoan);

            // Refresh loan from database to get updated status
            Loan refreshedLoan = loanRepository.findById(savedLoan.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

            // Send notification
            loanNotificationService.sendLoanApprovalNotification(refreshedLoan.getUser(), refreshedLoan);

            return ApiResponse.success(
                    "Loan approved and disbursed successfully",
                    loanMapper.toResponse(refreshedLoan)
            );
        } catch (Exception e) {
            log.error("Error approving loan: ", e);
            throw e;
        }
    }

    private ApiResponse<LoanResponse> rejectLoan(Loan loan, LoanApprovalRequest request, String adminEmail) {
        try {
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
        } catch (Exception e) {
            log.error("Error rejecting loan: ", e);
            throw e;
        }
    }

    @Transactional
    public void disburseLoan(Loan loan) {
        try {
            log.info("Starting loan disbursement for loan: {}", loan.getLoanNumber());

            // ✅ Step 1: Set disbursed date FIRST
            loan.setDisbursedDate(LocalDate.now());
            Loan updatedLoan = loanRepository.save(loan);
            log.info("Disbursed date set: {}", updatedLoan.getDisbursedDate());

            // ✅ Step 2: Generate EMI schedule (needs disbursedDate to be set)
            loanService.generateLoanSchedule(updatedLoan);
            log.info("EMI schedule generated for loan: {}", loan.getLoanNumber());

            // ✅ Step 3: Credit loan amount to account
            Account account = loan.getAccount();
            if (account == null) {
                throw new ResourceNotFoundException("Account not found for loan");
            }

            BigDecimal newBalance = account.getBalance().add(loan.getPrincipalAmount());
            account.setBalance(newBalance);
            accountRepository.save(account);
            log.info("Amount credited to account. New balance: ₹{}", newBalance);

            // ✅ Step 4: Update loan status to ACTIVE
            loan.setStatus(LoanStatus.ACTIVE);
            Loan finalLoan = loanRepository.save(loan);
            log.info("Loan status updated to ACTIVE: {}", finalLoan.getLoanNumber());

            // ✅ Step 5: Send disbursement notification
            loanNotificationService.sendLoanDisbursementNotification(finalLoan.getUser(), finalLoan);

        } catch (Exception e) {
            log.error("Error disbursing loan: {}", loan.getLoanNumber(), e);
            throw new RuntimeException("Failed to disburse loan: " + e.getMessage(), e);
        }
    }
}