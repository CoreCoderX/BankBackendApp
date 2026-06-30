package com.dvein.banking_backend.common.exception;

import com.dvein.banking_backend.common.constant.ErrorCodes;

public class OtpExpiredException extends CustomException {

    public OtpExpiredException() {
        super("OTP has expired", ErrorCodes.OTP_002);
    }
}