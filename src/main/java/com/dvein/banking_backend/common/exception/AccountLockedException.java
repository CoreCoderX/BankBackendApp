package com.dvein.banking_backend.common.exception;

import com.dvein.banking_backend.common.constant.ErrorCodes;

public class AccountLockedException extends CustomException {

    public AccountLockedException(String message) {
        super(message, ErrorCodes.AUTH_002);
    }

    public AccountLockedException() {
        super("Account locked due to multiple failed attempts", ErrorCodes.AUTH_002);
    }
}