package com.dvein.banking_backend.transaction.exception;

import com.dvein.banking_backend.common.exception.CustomException;

public class InvalidUpiIdException extends CustomException {

    public InvalidUpiIdException(String message) {
        super(message, "INVALID_UPI_ID");
    }

    public InvalidUpiIdException() {
        super("Invalid UPI ID", "INVALID_UPI_ID");
    }
}