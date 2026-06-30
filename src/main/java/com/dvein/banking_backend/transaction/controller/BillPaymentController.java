package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.BillPaymentRequest;
import com.dvein.banking_backend.transaction.dto.request.SaveBillerRequest;
import com.dvein.banking_backend.transaction.dto.response.BillPaymentResponse;
import com.dvein.banking_backend.transaction.dto.response.BillerResponse;
import com.dvein.banking_backend.transaction.service.BillPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
@Tag(name = "Bill Payment", description = "Bill payment endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping("/pay")
    @Operation(summary = "Pay bill", description = "Pay utility bills")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Transaction", description = "Bill payment")
    public ResponseEntity<ApiResponse<BillPaymentResponse>> payBill(
            @Valid @RequestBody BillPaymentRequest request,
            HttpServletRequest httpRequest) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        BillPaymentResponse response = billPaymentService.payBill(request, email, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Bill paid successfully", response));
    }

    @PostMapping("/biller")
    @Operation(summary = "Add biller", description = "Save biller for future payments")
    @RateLimited
    public ResponseEntity<ApiResponse<BillerResponse>> addBiller(@Valid @RequestBody SaveBillerRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        BillerResponse response = billPaymentService.addBiller(request, email);
        return ResponseEntity.ok(ApiResponse.success("Biller added successfully", response));
    }

    @GetMapping("/billers")
    @Operation(summary = "Get billers", description = "Get all saved billers")
    @RateLimited
    public ResponseEntity<ApiResponse<List<BillerResponse>>> getBillers() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<BillerResponse> response = billPaymentService.getMyBillers(email);
        return ResponseEntity.ok(ApiResponse.success("Billers retrieved successfully", response));
    }

    @DeleteMapping("/biller/{billerId}")
    @Operation(summary = "Delete biller", description = "Delete saved biller")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> deleteBiller(@PathVariable Long billerId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        billPaymentService.deleteBiller(billerId, email);
        return ResponseEntity.ok(ApiResponse.success("Biller deleted successfully", null));
    }
}