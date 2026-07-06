package com.dvein.banking_backend.transaction.exception;

import com.dvein.banking_backend.common.exception.CustomException;

public class UpiPinLockedException extends CustomException {

    public UpiPinLockedException(String message) {
        super(message, "UPI_PIN_LOCKED");
    }

    public UpiPinLockedException() {
        super("UPI PIN is locked due to multiple failed attempts", "UPI_PIN_LOCKED");
    }
}