package com.dvein.banking_backend.FundTransaction.validator;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.FundTransaction.exception.InsufficientBalanceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferValidator {

    public void validateAccountOwnership(Account account, Long userId) {
        if (account.getCustomer() == null ||
                account.getCustomer().getUser() == null ||
                !account.getCustomer().getUser().getId().equals(userId)) {
            throw new InvalidRequestException("You do not own this account");
        }
    }

    public void validateBalance(Account account, BigDecimal amount) {
        BigDecimal availableBalance = account.getBalance().subtract(account.getHoldBalance());

        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: ₹" + availableBalance
            );
        }
    }
}