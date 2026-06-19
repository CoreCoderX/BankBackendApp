package com.dvein.banking_backend.common.exception;

import com.dvein.banking_backend.common.constant.ErrorCodes;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(String message) {
        super(message, ErrorCodes.AUTH_005);
    }

    public UnauthorizedException() {
        super("Unauthorized access", ErrorCodes.AUTH_005);
    }
}