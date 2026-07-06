package com.dvein.banking_backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AccountNumberGenerator {

    @Value("${account.number.length}")
    private int accountNumberLength;

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateAccountNumber() {
        // Format: ACC + YYYYMMDD + Random 4 digits
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", RANDOM.nextInt(10000));

        return "ACC" + datePrefix + randomSuffix;
    }

    public String generateUniqueAccountNumber(java.util.function.Predicate<String> existsCheck) {
        String accountNumber;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            accountNumber = generateAccountNumber();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique account number after " + maxAttempts + " attempts");
            }
        } while (existsCheck.test(accountNumber));

        return accountNumber;
    }
}