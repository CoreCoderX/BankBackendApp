package com.dvein.banking_backend.common.exception;

public class DuplicateResourceException extends CustomException {

    public DuplicateResourceException(String resource, String field) {
        super(String.format("%s already exists with %s", resource, field), "DUPLICATE");
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE");
    }
}