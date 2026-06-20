package com.dvein.banking_backend.loan.exception;

public class LoanProcessingException extends RuntimeException {
    public LoanProcessingException(String message) {
        super(message);
    }
}