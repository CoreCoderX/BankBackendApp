package com.dvein.banking_backend.admin.controller;

import com.dvein.banking_backend.admin.dto.request.ApproveCreditCardRequest;
import com.dvein.banking_backend.admin.dto.request.RejectCreditCardRequest;
import com.dvein.banking_backend.card.service.CreditCardService;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin Card Management", description = "Admin card approval and management endpoints")
public class AdminCardController {

    private final CreditCardService creditCardService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping("/credit/{cardId}/approve")
    @Operation(summary = "Approve credit card", description = "Approve credit card application and generate card number")
    @RateLimited(limit = 50, duration = 60, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card approved")
    public ResponseEntity<ApiResponse<Void>> approveCreditCard(
            @PathVariable Long cardId,
            @Valid @RequestBody ApproveCreditCardRequest request,
            HttpServletRequest httpRequest) {

        String adminEmail = securityContextHelper.getCurrentUserEmail();

        creditCardService.approveCreditCard(
                cardId,
                request.getApprovedCreditLimit(),
                request.getInterestRate(),
                adminEmail
        );

        return ResponseEntity.ok(ApiResponse.success("Credit card approved and card number generated", null));
    }

    @PostMapping("/credit/{cardId}/reject")
    @Operation(summary = "Reject credit card", description = "Reject credit card application")
    @RateLimited(limit = 50, duration = 60, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card rejected")
    public ResponseEntity<ApiResponse<Void>> rejectCreditCard(
            @PathVariable Long cardId,
            @Valid @RequestBody RejectCreditCardRequest request,
            HttpServletRequest httpRequest) {

        String adminEmail = securityContextHelper.getCurrentUserEmail();

        creditCardService.rejectCreditCard(cardId, request.getReason(), adminEmail);

        return ResponseEntity.ok(ApiResponse.success("Credit card application rejected", null));
    }
}