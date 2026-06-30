package com.dvein.banking_backend.transaction.validation;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import com.dvein.banking_backend.common.enums.KycStatus;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.transaction.exception.InsufficientBalanceException;
import com.dvein.banking_backend.transaction.exception.TransactionLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private final AccountRepository accountRepository;

    public void validateSenderAccount(Account account) {
        if (account == null) {
            throw new InvalidRequestException("Sender account not found");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidRequestException("Sender account is not active. Status: " + account.getStatus());
        }

        Customer customer = account.getCustomer();
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new InvalidRequestException("Customer account is not active. Status: " + customer.getStatus());
        }
    }

    public void validateReceiverAccount(Account account) {
        if (account == null) {
            throw new InvalidRequestException("Receiver account not found");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidRequestException("Receiver account is not active");
        }
    }

    public void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %.2f, Required: %.2f",
                            account.getBalance(), amount));
        }
    }

    public void validateMinimumBalance(Account account, BigDecimal debitAmount) {
        BigDecimal balanceAfterDebit = account.getBalance().subtract(debitAmount);
        if (balanceAfterDebit.compareTo(account.getMinimumBalance()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Transaction will breach minimum balance requirement. Minimum: %.2f",
                            account.getMinimumBalance()));
        }
    }

    public void validateAmount(BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than zero");
        }

        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            throw new InvalidRequestException(
                    String.format("Amount must be at least %.2f", minAmount));
        }

        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            throw new TransactionLimitExceededException(
                    String.format("Amount cannot exceed %.2f", maxAmount));
        }
    }

    public void validateSameAccount(Account sender, Account receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new InvalidRequestException("Cannot transfer to the same account");
        }
    }

    public void validateIfscCode(String ifscCode) {
        if (ifscCode == null || !ifscCode.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            throw new InvalidRequestException("Invalid IFSC code format");
        }
    }

    public void validateUpiId(String upiId) {
        if (upiId == null || !upiId.matches("^[a-zA-Z0-9._-]+@[a-zA-Z]+$")) {
            throw new InvalidRequestException("Invalid UPI ID format");
        }
    }
}