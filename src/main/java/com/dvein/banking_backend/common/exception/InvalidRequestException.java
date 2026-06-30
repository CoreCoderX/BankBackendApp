package com.dvein.banking_backend.common.exception;

import com.dvein.banking_backend.common.constant.ErrorCodes;

public class InvalidRequestException extends CustomException {

    public InvalidRequestException(String message) {
        super(message, ErrorCodes.VAL_001);
    }
}