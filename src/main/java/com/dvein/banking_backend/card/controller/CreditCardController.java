package com.dvein.banking_backend.card.controller;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.card.dto.request.ApplyCreditCardRequest;
import com.dvein.banking_backend.card.dto.request.BlockCardRequest;
import com.dvein.banking_backend.card.dto.request.SetCardPinRequest;
import com.dvein.banking_backend.card.dto.response.CreditCardResponse;
import com.dvein.banking_backend.card.service.CreditCardService;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards/credit")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Credit Card Management", description = "Credit card management endpoints")
public class CreditCardController {

    private final CreditCardService creditCardService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    @Operation(summary = "Apply for credit card", description = "Submit credit card application")
    @RateLimited(limit = 3, duration = 86400, keyType = RateLimited.KeyType.USER,
            message = "Credit card application limit reached. Please try again tomorrow.")
    @Audited(action = AuditAction.CREATE, entityType = "CreditCard", description = "Credit card application submitted")
    public ResponseEntity<ApiResponse<CreditCardResponse>> applyCreditCard(
            @Valid @RequestBody ApplyCreditCardRequest request) {
        CreditCardResponse card = creditCardService.applyCreditCard(request.getAccountId(), request.getRequestedCreditLimit());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit card application submitted", card));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get credit cards", description = "Get all credit cards for account")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<CreditCardResponse>>> getCreditCards(
            @PathVariable Long accountId) {
        List<CreditCardResponse> cards = creditCardService.getAccountCreditCards(accountId);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PostMapping("/{cardId}/activate")
    @Operation(summary = "Activate credit card", description = "Activate credit card")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card activated")
    public ResponseEntity<ApiResponse<Void>> activateCreditCard(@PathVariable Long cardId) {
        creditCardService.activateCreditCard(cardId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CARD_ACTIVATED, null));
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Block credit card", description = "Block credit card")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.CARD_BLOCK, entityType = "CreditCard", description = "Credit card blocked")
    public ResponseEntity<ApiResponse<Void>> blockCreditCard(
            @PathVariable Long cardId,
            @Valid @RequestBody BlockCardRequest request) {
        creditCardService.blockCreditCard(cardId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CARD_BLOCKED, null));
    }

    @PostMapping("/{cardId}/unblock")
    @Operation(summary = "Unblock credit card",
            description = "Unblock previously blocked credit card")
    @RateLimited(limit = 10,
            duration = 3600,
            keyType = RateLimited.KeyType.USER)
    @Audited(
            action = AuditAction.UPDATE,
            entityType = "CreditCard",
            description = "Credit card unblocked"
    )
    public ResponseEntity<ApiResponse<Void>> unblockCreditCard(
            @PathVariable Long cardId) {

        creditCardService.unblockCreditCard(cardId);

        return ResponseEntity.ok(
                ApiResponse.success("Card unblocked successfully", null)
        );
    }

    @PostMapping("/{cardId}/set-pin")
    @Operation(summary = "Set card PIN", description = "Set PIN for credit card")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Card PIN set")
    public ResponseEntity<ApiResponse<Void>> setCardPin(
            @PathVariable Long cardId,
            @Valid @RequestBody SetCardPinRequest request) {
        creditCardService.setCardPin(cardId, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CARD_PIN_SET, null));
    }
}