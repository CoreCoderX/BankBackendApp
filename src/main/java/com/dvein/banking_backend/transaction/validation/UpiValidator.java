package com.dvein.banking_backend.transaction.validation;

import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.transaction.exception.InvalidUpiIdException;
import com.dvein.banking_backend.transaction.model.UpiId;
import com.dvein.banking_backend.transaction.repository.UpiIdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpiValidator {

    private final UpiIdRepository upiIdRepository;

    public void validateUpiIdFormat(String upiId) {
        if (upiId == null || upiId.isEmpty()) {
            throw new InvalidUpiIdException("UPI ID cannot be empty");
        }

        if (!upiId.matches("^[a-zA-Z0-9._-]+@[a-zA-Z]+$")) {
            throw new InvalidUpiIdException("Invalid UPI ID format. Expected: username@bank");
        }

        if (!upiId.endsWith("@dveinbank")) {
            throw new InvalidUpiIdException("UPI ID must end with @dveinbank");
        }
    }

    public void validateUpiIdExists(String upiId) {
        if (!upiIdRepository.existsByUpiId(upiId)) {
            throw new InvalidUpiIdException("UPI ID does not exist: " + upiId);
        }
    }

    public void validateUpiIdActive(UpiId upiId) {
        if (!upiId.isActive()) {
            throw new InvalidRequestException("UPI ID is not active");
        }

        if (!upiId.isVerified()) {
            throw new InvalidRequestException("UPI ID is not verified");
        }

        if (upiId.getLinkedAccount() == null) {
            throw new InvalidRequestException("UPI ID is not linked to any account");
        }
    }

    public void validateUpiHandle(String handle) {
        if (handle == null || handle.isEmpty()) {
            throw new InvalidRequestException("UPI handle cannot be empty");
        }

        if (!handle.matches("^[a-zA-Z0-9._-]+$")) {
            throw new InvalidRequestException(
                    "UPI handle can only contain letters, numbers, dots, hyphens, and underscores");
        }

        if (handle.length() < 3 || handle.length() > 30) {
            throw new InvalidRequestException("UPI handle must be between 3 and 30 characters");
        }
    }
}