package com.dvein.banking_backend.FundTransaction.exception;

public class TransferLimitExceededException extends RuntimeException {
    public TransferLimitExceededException(String message) {
        super(message);
    }
}