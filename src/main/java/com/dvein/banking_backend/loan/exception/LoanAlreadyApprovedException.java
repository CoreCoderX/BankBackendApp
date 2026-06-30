package com.dvein.banking_backend.loan.exception;

public class LoanAlreadyApprovedException extends RuntimeException {
    public LoanAlreadyApprovedException(String message) {
        super(message);
    }

    public LoanAlreadyApprovedException(String message, Throwable cause) {
        super(message, cause);
    }
}