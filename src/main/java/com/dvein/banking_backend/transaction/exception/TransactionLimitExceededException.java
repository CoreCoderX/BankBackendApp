package com.dvein.banking_backend.transaction.exception;

import com.dvein.banking_backend.common.exception.CustomException;

public class TransactionLimitExceededException extends CustomException {

    public TransactionLimitExceededException(String message) {
        super(message, "TRANSACTION_LIMIT_EXCEEDED");
    }

    public TransactionLimitExceededException() {
        super("Transaction limit exceeded", "TRANSACTION_LIMIT_EXCEEDED");
    }
}