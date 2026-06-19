package com.dvein.banking_backend.admin.controller;

import com.dvein.banking_backend.admin.dto.request.ApproveCreditCardRequest;
import com.dvein.banking_backend.admin.dto.request.RejectCreditCardRequest;
import com.dvein.banking_backend.card.service.CreditCardService;
import com.dvein.banking_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin Card Management", description = "Admin card approval and management endpoints")
public class AdminCardController {

    private final CreditCardService creditCardService;

    @PostMapping("/credit/{cardId}/approve")
    @Operation(summary = "Approve credit card", description = "Approve credit card application")
    public ResponseEntity<ApiResponse<Void>> approveCreditCard(
            @PathVariable Long cardId,
            @Valid @RequestBody ApproveCreditCardRequest request) {
        creditCardService.approveCreditCard(cardId, request.getApprovedCreditLimit());
        return ResponseEntity.ok(ApiResponse.success("Credit card approved successfully", null));
    }

    @PostMapping("/credit/{cardId}/reject")
    @Operation(summary = "Reject credit card", description = "Reject credit card application")
    public ResponseEntity<ApiResponse<Void>> rejectCreditCard(
            @PathVariable Long cardId,
            @Valid @RequestBody RejectCreditCardRequest request) {
        creditCardService.rejectCreditCard(cardId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Credit card rejected", null));
    }
}