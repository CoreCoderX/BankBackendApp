package com.dvein.banking_backend.loan.validator;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class LoanValidator {

    public void validateAccountOwnership(Account account, Long userId) {
        if (account.getCustomer() == null ||
                account.getCustomer().getUser() == null ||
                !account.getCustomer().getUser().getId().equals(userId)) {
            throw new InvalidRequestException("You do not own this account");
        }
    }
}