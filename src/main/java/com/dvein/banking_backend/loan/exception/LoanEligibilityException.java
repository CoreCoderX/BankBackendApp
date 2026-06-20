package com.dvein.banking_backend.loan.exception;

public class LoanEligibilityException extends RuntimeException {
    public LoanEligibilityException(String message) {
        super(message);
    }
}