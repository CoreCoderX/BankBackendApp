package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Kyc;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.KycRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.KycStatus;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.loan.dto.response.LoanEligibilityResponse;
import com.dvein.banking_backend.loan.exception.LoanEligibilityException;
import com.dvein.banking_backend.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanEligibilityService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final KycRepository kycRepository;
    private final SecurityContextHelper securityContextHelper;

    private static final int MAX_ACTIVE_LOANS = 3;
    private static final BigDecimal MIN_ACCOUNT_BALANCE = new BigDecimal("5000");

    public ApiResponse<LoanEligibilityResponse> checkEligibility(Long accountId) {
        Long userId = securityContextHelper.getCurrentUserId();

        List<String> checks = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getCustomer().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Account not found");
        }

        Customer customer = account.getCustomer();

        // ✅ FIXED: Check KYC using KycRepository
        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElse(null);

        if (kyc != null && kyc.getStatus() == KycStatus.VERIFIED) {
            checks.add("KYC Verified ✓");
        } else {
            failures.add("KYC not verified");
        }

        if (isAccountActive(account)) {
            checks.add("Account Active ✓");
        } else {
            failures.add("Account is not active");
        }

        Long activeLoanCount = loanRepository.countActiveLoansForUser(userId);
        if (activeLoanCount < MAX_ACTIVE_LOANS) {
            checks.add("Loan limit available (" + activeLoanCount + "/" + MAX_ACTIVE_LOANS + ") ✓");
        } else {
            failures.add("Maximum active loans reached (" + MAX_ACTIVE_LOANS + ")");
        }

        if (account.getBalance().compareTo(MIN_ACCOUNT_BALANCE) >= 0) {
            checks.add("Minimum balance maintained ✓");
        } else {
            failures.add("Minimum balance of ₹" + MIN_ACCOUNT_BALANCE + " required");
        }

        int creditScore = calculateMockCreditScore(userId, account);
        BigDecimal maxEligibleAmount = calculateMaxEligibleAmount(creditScore, account.getBalance());

        boolean isEligible = failures.isEmpty() && creditScore >= 650;

        if (creditScore >= 650) {
            checks.add("Credit Score: " + creditScore + " ✓");
        } else {
            failures.add("Credit score too low: " + creditScore + " (minimum 650 required)");
        }

        LoanEligibilityResponse response = LoanEligibilityResponse.builder()
                .isEligible(isEligible)
                .maxEligibleAmount(maxEligibleAmount)
                .creditScore(creditScore)
                .eligibilityChecks(checks)
                .failureReasons(failures)
                .build();

        return ApiResponse.success("Eligibility check completed", response);
    }

    public void validateEligibilityForApplication(Long userId, Account account, BigDecimal requestedAmount) {
        Customer customer = account.getCustomer();

        if (customer == null) {
            throw new LoanEligibilityException("Customer not found");
        }

        // ✅ FIXED: Check KYC using KycRepository
        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() ->
                        new LoanEligibilityException("KYC record not found"));

        if (kyc.getStatus() != KycStatus.VERIFIED) {
            throw new LoanEligibilityException(
                    "KYC verification required before applying for loan");
        }

        if (!isAccountActive(account)) {
            throw new LoanEligibilityException("Account must be active to apply for loan");
        }

        Long activeLoanCount = loanRepository.countActiveLoansForUser(userId);
        if (activeLoanCount >= MAX_ACTIVE_LOANS) {
            throw new LoanEligibilityException(
                    "Maximum active loans limit reached (" + MAX_ACTIVE_LOANS + ")"
            );
        }

        int creditScore = calculateMockCreditScore(userId, account);
        if (creditScore < 650) {
            throw new LoanEligibilityException(
                    "Credit score too low: " + creditScore + " (minimum 650 required)"
            );
        }

        BigDecimal maxEligible = calculateMaxEligibleAmount(creditScore, account.getBalance());
        if (requestedAmount.compareTo(maxEligible) > 0) {
            throw new LoanEligibilityException(
                    "Requested amount exceeds maximum eligible amount of ₹" + maxEligible
            );
        }
    }

    private boolean isAccountActive(Account account) {
        return account.getStatus() != null &&
                account.getStatus().toString().equals("ACTIVE");
    }

    private int calculateMockCreditScore(Long userId, Account account) {
        int baseScore = 700;

        if (account.getBalance().compareTo(new BigDecimal("100000")) >= 0) {
            baseScore += 50;
        } else if (account.getBalance().compareTo(new BigDecimal("50000")) >= 0) {
            baseScore += 25;
        }

        Long activeLoans = loanRepository.countActiveLoansForUser(userId);
        baseScore -= (int) (activeLoans * 20);

        return Math.min(Math.max(baseScore, 300), 900);
    }

    private BigDecimal calculateMaxEligibleAmount(int creditScore, BigDecimal accountBalance) {
        BigDecimal multiplier;

        if (creditScore >= 800) {
            multiplier = new BigDecimal("10");
        } else if (creditScore >= 750) {
            multiplier = new BigDecimal("7");
        } else if (creditScore >= 700) {
            multiplier = new BigDecimal("5");
        } else {
            multiplier = new BigDecimal("3");
        }

        return accountBalance.multiply(multiplier);
    }
}