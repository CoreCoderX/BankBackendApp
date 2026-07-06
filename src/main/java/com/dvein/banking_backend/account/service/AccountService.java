package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.CreateAccountRequest;
import com.dvein.banking_backend.account.dto.response.AccountResponse;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.constant.AppConstants;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userid", userId));

        // Check if customer already has max accounts
        long accountCount = accountRepository.countByCustomer(customer);
        if (accountCount >= 5) {
            throw new InvalidRequestException("Maximum account limit reached");
        }

        // Generate unique account number
        String accountNumber = accountNumberGenerator.generateUniqueAccountNumber(
                acc -> accountRepository.existsByAccountNumber(acc)
        );

        // Create account
        Account account = Account.builder()
                .customer(customer)
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .ifscCode(AppConstants.DEFAULT_IFSC_CODE)
                .branchCode(AppConstants.DEFAULT_BRANCH_CODE)
                .branchName("Main Branch")
                .balance(request.getInitialDeposit() != null ? BigDecimal.valueOf(request.getInitialDeposit()) : BigDecimal.ZERO)
                .minimumBalance(request.getAccountType().equals(com.dvein.banking_backend.common.enums.AccountType.SAVINGS) ?
                        BigDecimal.valueOf(1000) : BigDecimal.valueOf(5000))
                .primary(accountCount == 0) // First account is primary
                .build();

        account = accountRepository.save(account);

        log.info("Account created for customer: {} - Account Number: {}", userId, accountNumber);

        return mapToAccountResponse(account);
    }

    public AccountResponse getAccount(Long accountId, String email) {
        Account account = accountRepository
                .findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account",
                                "id",
                                accountId));

        return mapToAccountResponse(account);
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        return mapToAccountResponse(account);
    }

    public List<AccountResponse> getCustomerAccounts(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userid", userId));

        List<Account> accounts = accountRepository.findByCustomer(customer);

        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAccountStatus(Long accountId, AccountStatus status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setStatus(status);
        accountRepository.save(account);

        log.info("Account status updated: {} - Status: {}", accountId, status);
    }

    @Transactional
    public void setPrimaryAccount(Long userId, Long accountId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));

        // FIX IDOR: use scoped query — only fetches if the account belongs to this customer
        Account account = accountRepository.findByIdAndCustomerUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Remove primary from all existing accounts
        List<Account> allAccounts = accountRepository.findByCustomer(customer);
        allAccounts.forEach(acc -> acc.setPrimary(false));
        accountRepository.saveAll(allAccounts);

        // Set primary for this account
        account.setPrimary(true);
        accountRepository.save(account);

        log.info("Primary account set: {} for customer: {}", accountId, userId);
    }

    @Transactional
    public void closeAccount(Long accountId, String reason, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // FIX: BigDecimal.equals() considers scale — 0.00 != 0 (ZERO has scale 0).
        // Use compareTo instead, which only compares numeric value.
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidRequestException("Cannot close account with non-zero balance");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosureReason(reason);
        account.setClosedAt(java.time.LocalDateTime.now());
        accountRepository.save(account);

        log.info("Account closed: {} - Reason: {}", accountId, reason);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .ifscCode(account.getIfscCode())
                .branchCode(account.getBranchCode())
                .branchName(account.getBranchName())
                .balance(account.getBalance())
                .minimumBalance(account.getMinimumBalance())
                .status(account.getStatus())
                .primary(account.isPrimary())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}