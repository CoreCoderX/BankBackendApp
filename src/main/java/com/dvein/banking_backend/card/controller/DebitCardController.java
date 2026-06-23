package com.dvein.banking_backend.card.controller;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.card.dto.request.BlockCardRequest;
import com.dvein.banking_backend.card.dto.request.GenerateDebitCardRequest;
import com.dvein.banking_backend.card.dto.request.SetCardPinRequest;
import com.dvein.banking_backend.card.dto.response.DebitCardResponse;
import com.dvein.banking_backend.card.service.DebitCardService;
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
@RequestMapping("/cards/debit")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Debit Card Management", description = "Debit card management endpoints")
public class DebitCardController {

    private final DebitCardService debitCardService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/generate")
    @Operation(summary = "Generate debit card", description = "Generate debit card for account")
    @RateLimited(limit = 5, duration = 86400, keyType = RateLimited.KeyType.USER,
            message = "Card generation limit reached. Please contact support.")
    @Audited(action = AuditAction.CARD_ISSUE, entityType = "DebitCard", description = "Debit card generated")
    public ResponseEntity<ApiResponse<DebitCardResponse>> generateDebitCard(
            @Valid @RequestBody GenerateDebitCardRequest request) {

        String email = securityContextHelper.getCurrentUserEmailOrThrow();

        DebitCardResponse card = debitCardService.generateDebitCard(request.getAccountId(), email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.CARD_GENERATED, card));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get debit cards", description = "Get all debit cards for account")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<DebitCardResponse>>> getDebitCards(
            @PathVariable Long accountId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<DebitCardResponse> cards = debitCardService.getAccountDebitCards(accountId, email);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PostMapping("/{cardId}/activate")
    @Operation(summary = "Activate debit card", description = "Activate debit card")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "DebitCard", description = "Debit card activated")
    public ResponseEntity<ApiResponse<Void>> activateDebitCard(@PathVariable Long cardId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        debitCardService.activateDebitCard(cardId, email);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CARD_ACTIVATED, null));
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Block debit card", description = "Block debit card")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.CARD_BLOCK, entityType = "DebitCard", description = "Debit card blocked")
    public ResponseEntity<ApiResponse<Void>> blockDebitCard(
            @PathVariable Long cardId,
            @Valid @RequestBody BlockCardRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        debitCardService.blockDebitCard(cardId, email, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CARD_BLOCKED, null));
    }

    @PostMapping("/{cardId}/unblock")
    @Operation(summary = "Unblock debit card",
            description = "Unblock previously blocked debit card")
    @RateLimited(limit = 10,
            duration = 3600,
            keyType = RateLimited.KeyType.USER)
    @Audited(
            action = AuditAction.UPDATE,
            entityType = "DebitCard",
            description = "Debit card unblocked"
    )
    public ResponseEntity<ApiResponse<Void>> unblockDebitCard(
            @PathVariable Long cardId) {

        String email = securityContextHelper.getCurrentUserEmailOrThrow();

        debitCardService.unblockDebitCard(cardId, email);

        return ResponseEntity.ok(
                ApiResponse.success("Card unblocked successfully", null)
        );
    }

    @PostMapping("/{cardId}/set-pin")
    @Operation(summary = "Set card PIN", description = "Set PIN for debit card")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "DebitCard", description = "Card PIN set")
    public ResponseEntity<ApiResponse<Void>> setCardPin(
            @PathVariable Long cardId,
            @Valid @RequestBody SetCardPinRequest request) {

        String email = securityContextHelper.getCurrentUserEmailOrThrow();

        debitCardService.setCardPin(cardId, email, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CARD_PIN_SET, null));
    }
}