package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.loan.dto.request.ApplyLoanRequest;
import com.dvein.banking_backend.loan.dto.response.LoanDetailResponse;
import com.dvein.banking_backend.loan.dto.response.LoanResponse;
import com.dvein.banking_backend.loan.dto.response.LoanScheduleResponse;
import com.dvein.banking_backend.loan.dto.response.OutstandingBalanceResponse;
import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.mapper.LoanMapper;
import com.dvein.banking_backend.loan.mapper.LoanScheduleMapper;
import com.dvein.banking_backend.loan.model.Loan;
import com.dvein.banking_backend.loan.model.LoanSchedule;
import com.dvein.banking_backend.loan.repository.LoanPenaltyRepository;
import com.dvein.banking_backend.loan.repository.LoanRepository;
import com.dvein.banking_backend.loan.repository.LoanScheduleRepository;
import com.dvein.banking_backend.loan.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final EmiCalculatorService emiCalculatorService;
    private final LoanEligibilityService loanEligibilityService;
    private final LoanValidator loanValidator;
    private final LoanMapper loanMapper;
    private final LoanScheduleMapper loanScheduleMapper;
    private final LoanNotificationService loanNotificationService;
    private final SecurityContextHelper securityContextHelper;

    @Transactional
    public ApiResponse<LoanResponse> applyLoan(ApplyLoanRequest request) {
        try {
            // ✅ FIXED: Now using static method
            Long userId = securityContextHelper.getCurrentUserId();

            if (userId == null) {
                throw new ResourceNotFoundException("User ID not found in security context");
            }

            log.info("Loan application received from user: {}", userId);

            if (request == null || request.getAccountId() == null) {
                throw new ResourceNotFoundException("Account ID is required");
            }

            if (request.getAccountId() <= 0) {
                throw new ResourceNotFoundException("Invalid account ID. Please provide a valid account.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            log.info("User found: {}", user.getEmail());

            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

            // ✅ Ownership validation
            if (!account.getCustomer().getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Account not found");
            }

            log.info("Account found and ownership verified: {}", account.getId());

            loanValidator.validateAccountOwnership(account, userId);
            log.info("Account ownership validated");

            loanEligibilityService.validateEligibilityForApplication(
                    userId, account, request.getPrincipalAmount()
            );
            log.info("Loan eligibility validated");

            BigDecimal emi = emiCalculatorService.calculateEmi(
                    request.getPrincipalAmount(),
                    request.getInterestRate(),
                    request.getTenureMonths()
            );
            log.info("EMI calculated: {}", emi);

            BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(request.getTenureMonths()));
            BigDecimal totalInterest = totalPayable.subtract(request.getPrincipalAmount());

            Loan loan = Loan.builder()
                    .user(user)
                    .account(account)
                    .loanType(request.getLoanType())
                    .principalAmount(request.getPrincipalAmount())
                    .interestRate(request.getInterestRate())
                    .tenureMonths(request.getTenureMonths())
                    .emiAmount(emi)
                    .remainingPrincipal(request.getPrincipalAmount())
                    .totalInterest(totalInterest)
                    .totalPayable(totalPayable)
                    .amountPaid(BigDecimal.ZERO)
                    .status(LoanStatus.PENDING)
                    .appliedDate(LocalDate.now())
                    .purpose(request.getPurpose())
                    .createdBy(user.getEmail())
                    .build();

            log.info("Loan object created, saving to database");
            Loan savedLoan = loanRepository.save(loan);
            log.info("Loan saved successfully: {}", savedLoan.getLoanNumber());

            loanNotificationService.sendLoanApplicationNotification(user, savedLoan);

            return ApiResponse.success(
                    "Loan application submitted successfully. Awaiting admin approval.",
                    loanMapper.toResponse(savedLoan)
            );
        } catch (ResourceNotFoundException e) {
            log.warn("Validation error in applyLoan: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in applyLoan: ", e);
            throw e;
        }
    }

    public ApiResponse<LoanDetailResponse> getLoanById(Long loanId) {
        // ✅ FIXED: Static method call
        Long userId = securityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findWithDetailsById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Loan not found");
        }

        return ApiResponse.success(
                "Loan details retrieved successfully",
                loanMapper.toDetailResponse(loan)
        );
    }

    public ApiResponse<PageResponse<LoanResponse>> getAllLoans(int page, int size) {
        // ✅ FIXED: Static method call
        Long userId = securityContextHelper.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Loan> loanPage = loanRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<LoanResponse> responses = loanPage.getContent().stream()
                .map(loanMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<LoanResponse> pageResponse = PageResponse.<LoanResponse>builder()
                .content(responses)
                .pageNumber(loanPage.getNumber())
                .pageSize(loanPage.getSize())
                .totalElements(loanPage.getTotalElements())
                .totalPages(loanPage.getTotalPages())
                .last(loanPage.isLast())
                .build();

        return ApiResponse.success("Loans retrieved successfully", pageResponse);
    }

    public ApiResponse<List<LoanScheduleResponse>> getLoanSchedule(Long loanId) {
        // ✅ FIXED: Static method call
        Long userId = securityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Loan not found");
        }

        List<LoanSchedule> schedules = loanScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loanId);

        List<LoanScheduleResponse> responses = schedules.stream()
                .map(loanScheduleMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Loan schedule retrieved successfully", responses);
    }

    public ApiResponse<OutstandingBalanceResponse> getOutstandingBalance(Long loanId) {
        // ✅ FIXED: Static method call
        Long userId = securityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Loan not found");
        }

        Long emisPaid = loanScheduleRepository.countPaidEmis(loanId);
        if (emisPaid == null) {
            emisPaid = 0L;
        }

        Long emisRemaining = loanScheduleRepository.countPendingEmis(loanId);
        if (emisRemaining == null) {
            emisRemaining = 0L;
        }

        LoanSchedule nextEmi = null;
        List<LoanSchedule> pendingEmis = loanScheduleRepository.findNextPendingEmis(loanId);
        if (pendingEmis != null && !pendingEmis.isEmpty()) {
            nextEmi = pendingEmis.get(0);
            log.debug("Next EMI found: EMI #{}, Due Date: {}", nextEmi.getEmiNumber(), nextEmi.getDueDate());
        } else {
            log.debug("No pending EMIs found for loan: {}", loanId);
        }

        BigDecimal penaltyDue = loanPenaltyRepository.getUnpaidPenaltyForLoan(loanId);
        if (penaltyDue == null) {
            penaltyDue = BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, java.math.RoundingMode.HALF_UP);
        BigDecimal interestDue = loan.getRemainingPrincipal().multiply(monthlyRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal totalOutstanding = loan.getRemainingPrincipal()
                .add(interestDue)
                .add(penaltyDue);

        OutstandingBalanceResponse response = OutstandingBalanceResponse.builder()
                .loanNumber(loan.getLoanNumber())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .interestDue(interestDue)
                .penaltyDue(penaltyDue)
                .totalOutstanding(totalOutstanding)
                .emisRemaining(emisRemaining.intValue())
                .emisPaid(emisPaid.intValue())
                .nextEmiDate(nextEmi != null ? nextEmi.getDueDate() : null)
                .nextEmiAmount(nextEmi != null ? nextEmi.getEmiAmount() : null)
                .build();

        return ApiResponse.success("Outstanding balance retrieved successfully", response);
    }

    @Transactional
    public void generateLoanSchedule(Loan loan) {
        try {
            log.info("Generating loan schedule for loan: {}", loan.getLoanNumber());

            if (loan.getDisbursedDate() == null) {
                throw new RuntimeException("Loan disbursement date must be set before generating schedule");
            }

            List<com.dvein.banking_backend.loan.dto.response.EmiCalculationResponse.AmortizationEntry> amortization =
                    emiCalculatorService.generateAmortizationSchedule(
                            loan.getPrincipalAmount(),
                            loan.getInterestRate(),
                            loan.getTenureMonths(),
                            loan.getEmiAmount()
                    );

            LocalDate firstEmiDate = loan.getDisbursedDate().plusMonths(1);
            loan.setFirstEmiDate(firstEmiDate);
            loanRepository.save(loan);
            log.info("First EMI date set to: {}", firstEmiDate);

            for (var entry : amortization) {
                LoanSchedule schedule = LoanSchedule.builder()
                        .loan(loan)
                        .emiNumber(entry.getEmiNumber())
                        .dueDate(firstEmiDate.plusMonths(entry.getEmiNumber() - 1))
                        .emiAmount(entry.getEmiAmount())
                        .principalComponent(entry.getPrincipalComponent())
                        .interestComponent(entry.getInterestComponent())
                        .outstandingPrincipal(entry.getOutstandingBalance())
                        .status(com.dvein.banking_backend.loan.enums.RepaymentStatus.PENDING)
                        .build();

                loanScheduleRepository.save(schedule);
                log.debug("EMI {} saved with due date: {}", entry.getEmiNumber(), schedule.getDueDate());
            }

            log.info("Loan schedule generated successfully for loan: {}", loan.getLoanNumber());
        } catch (Exception e) {
            log.error("Error generating loan schedule for loan: {}", loan.getLoanNumber(), e);
            throw new RuntimeException("Failed to generate loan schedule: " + e.getMessage(), e);
        }
    }
}