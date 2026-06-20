package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
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

    private static final int MAX_ACTIVE_LOANS = 3;
    private static final BigDecimal MIN_ACCOUNT_BALANCE = new BigDecimal("5000");

    public ApiResponse<LoanEligibilityResponse> checkEligibility(Long accountId) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        List<String> checks = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Customer customer = account.getCustomer();

        // Check 1: KYC Verification
        if (customer != null && isKycVerified(customer)) {
            checks.add("KYC Verified ✓");
        } else {
            failures.add("KYC not verified");
        }

        // Check 2: Account Active
        if (isAccountActive(account)) {
            checks.add("Account Active ✓");
        } else {
            failures.add("Account is not active");
        }

        // Check 3: Existing Active Loans
        Long activeLoanCount = loanRepository.countActiveLoansForUser(userId);
        if (activeLoanCount < MAX_ACTIVE_LOANS) {
            checks.add("Loan limit available (" + activeLoanCount + "/" + MAX_ACTIVE_LOANS + ") ✓");
        } else {
            failures.add("Maximum active loans reached (" + MAX_ACTIVE_LOANS + ")");
        }

        // Check 4: Minimum Balance
        if (account.getBalance().compareTo(MIN_ACCOUNT_BALANCE) >= 0) {
            checks.add("Minimum balance maintained ✓");
        } else {
            failures.add("Minimum balance of ₹" + MIN_ACCOUNT_BALANCE + " required");
        }

        // Mock credit score
        int creditScore = calculateMockCreditScore(userId, account);

        // Calculate max eligible amount (mock logic)
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

        // KYC Check
        if (customer == null || !isKycVerified(customer)) {
            throw new LoanEligibilityException("KYC verification required before applying for loan");
        }

        // Account Active Check
        if (!isAccountActive(account)) {
            throw new LoanEligibilityException("Account must be active to apply for loan");
        }

        // Active Loan Limit
        Long activeLoanCount = loanRepository.countActiveLoansForUser(userId);
        if (activeLoanCount >= MAX_ACTIVE_LOANS) {
            throw new LoanEligibilityException(
                    "Maximum active loans limit reached (" + MAX_ACTIVE_LOANS + ")"
            );
        }

        // Credit Score Check
        int creditScore = calculateMockCreditScore(userId, account);
        if (creditScore < 650) {
            throw new LoanEligibilityException(
                    "Credit score too low: " + creditScore + " (minimum 650 required)"
            );
        }

        // Max eligible amount check
        BigDecimal maxEligible = calculateMaxEligibleAmount(creditScore, account.getBalance());
        if (requestedAmount.compareTo(maxEligible) > 0) {
            throw new LoanEligibilityException(
                    "Requested amount exceeds maximum eligible amount of ₹" + maxEligible
            );
        }
    }

    /**
     * Helper method to check KYC status - handles different field names
     */
    private boolean isKycVerified(Customer customer) {
        try {
            // Try different possible method names
            if (customer.getClass().getMethod("getIsKycVerified") != null) {
                return Boolean.TRUE.equals((Boolean) customer.getClass()
                        .getMethod("getIsKycVerified").invoke(customer));
            }
        } catch (Exception e) {
            // Method not found
        }

        try {
            if (customer.getClass().getMethod("getKycVerified") != null) {
                return Boolean.TRUE.equals((Boolean) customer.getClass()
                        .getMethod("getKycVerified").invoke(customer));
            }
        } catch (Exception e) {
            // Method not found
        }

        try {
            if (customer.getClass().getMethod("isKycVerified") != null) {
                return Boolean.TRUE.equals((Boolean) customer.getClass()
                        .getMethod("isKycVerified").invoke(customer));
            }
        } catch (Exception e) {
            // Method not found
        }

        // Default to false if no method found
        log.warn("Could not determine KYC status for customer");
        return false;
    }

    /**
     * Helper method to check account active status - handles different field names
     */
    private boolean isAccountActive(Account account) {
        try {
            if (account.getClass().getMethod("getIsActive") != null) {
                Boolean isActive = (Boolean) account.getClass().getMethod("getIsActive").invoke(account);
                return Boolean.TRUE.equals(isActive);
            }
        } catch (Exception e) {
            // Method not found
        }

        try {
            if (account.getClass().getMethod("getActive") != null) {
                Boolean isActive = (Boolean) account.getClass().getMethod("getActive").invoke(account);
                return Boolean.TRUE.equals(isActive);
            }
        } catch (Exception e) {
            // Method not found
        }

        try {
            if (account.getClass().getMethod("isActive") != null) {
                Boolean isActive = (Boolean) account.getClass().getMethod("isActive").invoke(account);
                return Boolean.TRUE.equals(isActive);
            }
        } catch (Exception e) {
            // Method not found
        }

        // Default to false if no method found
        log.warn("Could not determine active status for account");
        return false;
    }

    private int calculateMockCreditScore(Long userId, Account account) {
        // Mock credit score calculation
        // In production, integrate with CIBIL/Experian/Equifax
        int baseScore = 700;

        // Add points based on balance
        if (account.getBalance().compareTo(new BigDecimal("100000")) >= 0) {
            baseScore += 50;
        } else if (account.getBalance().compareTo(new BigDecimal("50000")) >= 0) {
            baseScore += 25;
        }

        // Reduce points for active loans
        Long activeLoans = loanRepository.countActiveLoansForUser(userId);
        baseScore -= (int) (activeLoans * 20);

        return Math.min(Math.max(baseScore, 300), 900);
    }

    private BigDecimal calculateMaxEligibleAmount(int creditScore, BigDecimal accountBalance) {
        // Mock logic: max eligible = balance × multiplier based on credit score
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