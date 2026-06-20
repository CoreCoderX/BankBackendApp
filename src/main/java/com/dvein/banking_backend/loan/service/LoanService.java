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

    @Transactional
    public ApiResponse<LoanResponse> applyLoan(ApplyLoanRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Validate account ownership
        loanValidator.validateAccountOwnership(account, userId);

        // Check eligibility
        loanEligibilityService.validateEligibilityForApplication(
                userId, account, request.getPrincipalAmount()
        );

        // Calculate EMI
        BigDecimal emi = emiCalculatorService.calculateEmi(
                request.getPrincipalAmount(),
                request.getInterestRate(),
                request.getTenureMonths()
        );

        BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(request.getTenureMonths()));
        BigDecimal totalInterest = totalPayable.subtract(request.getPrincipalAmount());

        // Create loan application
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

        Loan savedLoan = loanRepository.save(loan);

        log.info("Loan application created: {}", savedLoan.getLoanNumber());

        // Send notification
        loanNotificationService.sendLoanApplicationNotification(user, savedLoan);

        return ApiResponse.success(
                "Loan application submitted successfully",
                loanMapper.toResponse(savedLoan)
        );
    }

    public ApiResponse<LoanDetailResponse> getLoanById(Long loanId) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        return ApiResponse.success(
                "Loan details retrieved successfully",
                loanMapper.toDetailResponse(loan)
        );
    }

    public ApiResponse<PageResponse<LoanResponse>> getAllLoans(int page, int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();

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
        Long userId = SecurityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        List<LoanSchedule> schedules = loanScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loanId);

        List<LoanScheduleResponse> responses = schedules.stream()
                .map(loanScheduleMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Loan schedule retrieved successfully", responses);
    }

    public ApiResponse<OutstandingBalanceResponse> getOutstandingBalance(Long loanId) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Loan loan = loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Long emisPaid = loanScheduleRepository.countPaidEmis(loanId);
        Long emisRemaining = loanScheduleRepository.countPendingEmis(loanId);

        LoanSchedule nextEmi = loanScheduleRepository.findNextPendingEmi(loanId).orElse(null);

        BigDecimal penaltyDue = loanPenaltyRepository.getUnpaidPenaltyForLoan(loanId);
        if (penaltyDue == null) {
            penaltyDue = BigDecimal.ZERO;
        }

        // Calculate interest due on remaining principal
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
        List<com.dvein.banking_backend.loan.dto.response.EmiCalculationResponse.AmortizationEntry> amortization =
                emiCalculatorService.generateAmortizationSchedule(
                        loan.getPrincipalAmount(),
                        loan.getInterestRate(),
                        loan.getTenureMonths(),
                        loan.getEmiAmount()
                );

        LocalDate firstEmiDate = loan.getDisbursedDate().plusMonths(1);
        loan.setFirstEmiDate(firstEmiDate);

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
        }

        loanRepository.save(loan);
        log.info("Loan schedule generated for loan: {}", loan.getLoanNumber());
    }
}