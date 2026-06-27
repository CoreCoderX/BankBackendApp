package com.dvein.banking_backend.transaction.validator;

import com.dvein.banking_backend.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class BeneficiaryValidator {

    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{10,18}");

    public void validateIfscCode(String ifscCode) {
        if (!IFSC_PATTERN.matcher(ifscCode).matches()) {
            throw new InvalidRequestException("Invalid IFSC code format");
        }
    }

    public void validateAccountNumber(String accountNumber) {
        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new InvalidRequestException("Invalid account number format");
        }
    }
}