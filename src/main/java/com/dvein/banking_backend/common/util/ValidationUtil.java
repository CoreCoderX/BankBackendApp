package com.dvein.banking_backend.common.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[6-9]\\d{9}$" // Indian mobile number
    );

    private static final Pattern PAN_PATTERN = Pattern.compile(
            "^[A-Z]{5}[0-9]{4}[A-Z]{1}$"
    );

    private static final Pattern AADHAAR_PATTERN = Pattern.compile(
            "^[2-9]{1}[0-9]{11}$"
    );

    private static final Pattern IFSC_PATTERN = Pattern.compile(
            "^[A-Z]{4}0[A-Z0-9]{6}$"
    );

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public boolean isValidPAN(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan.toUpperCase()).matches();
    }

    public boolean isValidAadhaar(String aadhaar) {
        return aadhaar != null && AADHAAR_PATTERN.matcher(aadhaar).matches();
    }

    public boolean isValidIFSC(String ifsc) {
        return ifsc != null && IFSC_PATTERN.matcher(ifsc.toUpperCase()).matches();
    }

    public boolean isValidAmount(Double amount) {
        return amount != null && amount > 0;
    }

    public String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[<>\"']", "");
    }
}