package com.dvein.banking_backend.common.exception;

import com.dvein.banking_backend.common.constant.ErrorCodes;

public class InvalidOtpException extends CustomException {

    public InvalidOtpException() {
        super("Invalid OTP", ErrorCodes.OTP_001);
    }
}