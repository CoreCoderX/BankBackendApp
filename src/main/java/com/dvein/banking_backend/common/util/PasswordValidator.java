package com.dvein.banking_backend.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    @Value("${security.password.min-length}")
    private int minLength;

    @Value("${security.password.require-uppercase}")
    private boolean requireUppercase;

    @Value("${security.password.require-lowercase}")
    private boolean requireLowercase;

    @Value("${security.password.require-digit}")
    private boolean requireDigit;

    @Value("${security.password.require-special-char}")
    private boolean requireSpecialChar;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < minLength) {
            errors.add("Password must be at least " + minLength + " characters long");
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).matches()) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).matches()) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (requireDigit && !DIGIT_PATTERN.matcher(password).matches()) {
            errors.add("Password must contain at least one digit");
        }

        if (requireSpecialChar && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            errors.add("Password must contain at least one special character");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
}