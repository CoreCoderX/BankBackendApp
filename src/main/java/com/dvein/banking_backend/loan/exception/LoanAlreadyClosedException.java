package com.dvein.banking_backend.loan.exception;

public class LoanAlreadyClosedException extends RuntimeException {
    public LoanAlreadyClosedException(String message) {
        super(message);
    }

    public LoanAlreadyClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}