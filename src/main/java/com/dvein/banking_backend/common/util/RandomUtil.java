package com.dvein.banking_backend.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class RandomUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }

    public String generateAlphanumeric(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return result.toString();
    }

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public int generateRandomNumber(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
}