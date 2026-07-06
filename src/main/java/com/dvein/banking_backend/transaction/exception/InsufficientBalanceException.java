package com.dvein.banking_backend.transaction.exception;

import com.dvein.banking_backend.common.exception.CustomException;

public class InsufficientBalanceException extends CustomException {

    public InsufficientBalanceException(String message) {
        super(message, "INSUFFICIENT_BALANCE");
    }

    public InsufficientBalanceException() {
        super("Insufficient balance in account", "INSUFFICIENT_BALANCE");
    }
}