package com.dvein.banking_backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;

@Component
public class CardNumberGenerator {

    @Value("${card.debit.number-length}")
    private int cardNumberLength;

    @Value("${card.cvv-length}")
    private int cvvLength;

    @Value("${card.pin-length}")
    private int pinLength;

    @Value("${card.expiry-years}")
    private int expiryYears;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CARD_PREFIX = "4532"; // Visa BIN

    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_PREFIX);

        // Generate remaining digits (16 total - 4 prefix = 12 remaining)
        for (int i = 0; i < 12; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        return cardNumber.toString();
    }

    public String generateCVV() {
        return String.format("%0" + cvvLength + "d", RANDOM.nextInt((int) Math.pow(10, cvvLength)));
    }

    public LocalDate generateExpiryDate() {
        return LocalDate.now().plusYears(expiryYears);
    }

    public String formatCardNumber(String cardNumber) {
        // Format: 4532 1234 5678 9012
        return cardNumber.replaceAll("(.{4})", "$1 ").trim();
    }

    public String maskCardNumber(String cardNumber) {
        // Format: 4532 **** **** 9012
        if (cardNumber.length() < 16) {
            return cardNumber;
        }
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(12);
    }

    public String generateUniqueCardNumber(java.util.function.Predicate<String> existsCheck) {
        String cardNumber;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            cardNumber = generateCardNumber();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique card number after " + maxAttempts + " attempts");
            }
        } while (existsCheck.test(cardNumber));

        return cardNumber;
    }
}