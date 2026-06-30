package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.InternalTransferRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.service.InternalTransferService;
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
@RequestMapping("/transactions/internal")
@RequiredArgsConstructor
@Tag(name = "Internal Transfer", description = "Internal bank transfer endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class InternalTransferController {

    private final InternalTransferService internalTransferService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping
    @Operation(summary = "Internal transfer", description = "Transfer money to another account within the same bank")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Transaction", description = "Internal transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @Valid @RequestBody InternalTransferRequest request,
            HttpServletRequest httpRequest) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse response = internalTransferService.transferMoney(request, email, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Transfer initiated successfully", response));
    }
}