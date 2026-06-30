package com.dvein.banking_backend.transaction.exception;

import com.dvein.banking_backend.common.exception.CustomException;

public class FraudDetectedException extends CustomException {

    public FraudDetectedException(String message) {
        super(message, "FRAUD_DETECTED");
    }

    public FraudDetectedException() {
        super("Suspicious activity detected. Transaction blocked.", "FRAUD_DETECTED");
    }
}