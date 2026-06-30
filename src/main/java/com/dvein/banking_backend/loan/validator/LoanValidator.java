package com.dvein.banking_backend.loan.validator;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoanValidator {

    public void validateAccountOwnership(Account account, Long userId) {
        log.debug("Validating account ownership for accountId: {}, userId: {}",
                account.getId(), userId);

        // Check if account exists
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        // Check if customer is associated with account
        Customer customer = account.getCustomer();
        if (customer == null) {
            log.warn("Account {} has no customer associated", account.getId());
            throw new ResourceNotFoundException("Account has no customer information");
        }

        // Check if user is associated with customer
        User user = customer.getUser();
        if (user == null) {
            log.warn("Customer {} has no user associated", customer.getId());
            throw new ResourceNotFoundException("Invalid customer information");
        }

        // Check if user ID matches
        if (!user.getId().equals(userId)) {
            log.warn("User {} attempted to access account {} belonging to user {}",
                    userId, account.getId(), user.getId());
            throw new ResourceNotFoundException("You do not own this account");
        }

        // Additional validations
        validateAccountStatus(account);
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() == null) {
            throw new ResourceNotFoundException("Account has invalid status");
        }

        String status = account.getStatus().toString();
        if ("CLOSED".equals(status) || "INACTIVE".equals(status)) {
            throw new ResourceNotFoundException(
                    "Cannot apply for loan with a " + status + " account"
            );
        }
    }
}