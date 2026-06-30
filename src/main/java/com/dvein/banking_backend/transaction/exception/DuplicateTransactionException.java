package com.dvein.banking_backend.transaction.exception;

import com.dvein.banking_backend.common.exception.CustomException;

public class DuplicateTransactionException extends CustomException {

    public DuplicateTransactionException(String message) {
        super(message, "DUPLICATE_TRANSACTION");
    }

    public DuplicateTransactionException() {
        super("Duplicate transaction detected. Please try again later.", "DUPLICATE_TRANSACTION");
    }
}