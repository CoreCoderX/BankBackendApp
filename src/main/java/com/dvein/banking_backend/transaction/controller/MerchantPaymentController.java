package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.MerchantPaymentRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.service.MerchantPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
@Tag(name = "Merchant Payment", description = "Merchant payment endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class MerchantPaymentController {

    private final MerchantPaymentService merchantPaymentService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping("/pay")
    @Operation(summary = "Pay merchant", description = "Make payment to merchant")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Transaction", description = "Merchant payment")
    public ResponseEntity<ApiResponse<TransactionResponse>> payMerchant(
            @Valid @RequestBody MerchantPaymentRequest request,
            HttpServletRequest httpRequest) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse response = merchantPaymentService.payMerchant(request, email, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment successful", response));
    }
}