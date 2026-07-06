package com.dvein.banking_backend.transaction.validation;

import com.dvein.banking_backend.account.model.Beneficiary;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryValidator {

    public void validateBeneficiaryActive(Beneficiary beneficiary) {
        if (beneficiary == null) {
            throw new InvalidRequestException("Beneficiary not found");
        }

        if (!beneficiary.isVerified()) {
            throw new InvalidRequestException("Beneficiary is not verified. Please wait for verification.");
        }
    }

    public void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new InvalidRequestException("Account number is required");
        }

        if (accountNumber.length() < 8 || accountNumber.length() > 20) {
            throw new InvalidRequestException("Invalid account number length");
        }
    }
}