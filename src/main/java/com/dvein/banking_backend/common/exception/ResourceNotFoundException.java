package com.dvein.banking_backend.common.exception;

public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value), "NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}