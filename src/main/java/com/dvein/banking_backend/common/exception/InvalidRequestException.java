package com.dvein.banking_backend.common.exception;

public class InvalidRequestException extends CustomException {

    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }

    public InvalidRequestException(String message, String errorCode) {
        super(message, errorCode);
    }
}