package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Kyc;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.KycRepository;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import com.dvein.banking_backend.common.enums.KycStatus;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.exception.DuplicateTransactionException;
import com.dvein.banking_backend.transaction.exception.InsufficientBalanceException;
import com.dvein.banking_backend.transaction.model.TransactionLimit;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionValidationService {

    private final TransactionValidator transactionValidator;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final KycRepository kycRepository;
    private final TransactionLimitService limitService;

    public void validateIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            throw new InvalidRequestException("Idempotency key is required");
        }

        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateTransactionException(
                    "Duplicate transaction detected. This request has already been processed.");
        }
    }

    public void validateCustomerStatus(Customer customer) {
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new InvalidRequestException(
                    "Customer account is " + customer.getStatus() + ". Cannot perform transactions.");
        }
    }

    public void validateKycStatus(Customer customer) {
        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new InvalidRequestException("KYC not found. Please complete KYC verification."));

        if (kyc.getStatus() != KycStatus.VERIFIED) {
            throw new InvalidRequestException(
                    "KYC is not verified. Status: " + kyc.getStatus() + ". Cannot perform transactions.");
        }
    }

    public void validateSenderAccount(Account account) {
        transactionValidator.validateSenderAccount(account);
    }

    public void validateReceiverAccount(Account account) {
        transactionValidator.validateReceiverAccount(account);
    }

    public void validateSufficientBalance(Account account, BigDecimal totalAmount) {
        transactionValidator.validateSufficientBalance(account, totalAmount);
        transactionValidator.validateMinimumBalance(account, totalAmount);
    }

    public void validateTransactionAmount(BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
        transactionValidator.validateAmount(amount, minAmount, maxAmount);
    }

    public void validateNotSameAccount(Account sender, Account receiver) {
        transactionValidator.validateSameAccount(sender, receiver);
    }

    public void validateTransactionLimit(Customer customer, String transactionType, BigDecimal amount) {
        TransactionLimit limit = limitService.getOrCreateLimit(customer);

        // Validation is done in limitService.validateAndUpdateLimit
        // This is a pre-check
        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw new InvalidRequestException(
                    String.format("Amount exceeds per-transaction limit of %.2f", limit.getPerTransactionLimit()));
        }
    }

    public Account getAndValidateSenderAccount(Long accountId, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        validateSenderAccount(account);
        validateCustomerStatus(account.getCustomer());
        validateKycStatus(account.getCustomer());

        return account;
    }

    public Account getAndValidateReceiverAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        validateReceiverAccount(account);
        return account;
    }
}