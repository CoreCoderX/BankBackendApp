package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.*;
import com.dvein.banking_backend.transaction.dto.response.*;
import com.dvein.banking_backend.transaction.service.*;
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
@RequestMapping("/upi")
@RequiredArgsConstructor
@Tag(name = "UPI", description = "UPI payment endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class UpiController {

    private final UpiService upiService;
    private final UpiPinService upiPinService;
    private final UpiQrService upiQrService;
    private final UpiCollectRequestService collectRequestService;
    private final UpiTransactionService upiTransactionService;
    private final SecurityContextHelper securityContextHelper;

    // Profile Management
    @PostMapping("/profile")
    @Operation(summary = "Create UPI profile", description = "Create UPI profile for customer")
    @RateLimited
    public ResponseEntity<ApiResponse<UpiProfileResponse>> createProfile() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiProfileResponse response = upiService.createUpiProfile(email);
        return ResponseEntity.ok(ApiResponse.success("UPI profile created successfully", response));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get UPI profile", description = "Get UPI profile details")
    @RateLimited
    public ResponseEntity<ApiResponse<UpiProfileResponse>> getProfile() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiProfileResponse response = upiService.getUpiProfile(email);
        return ResponseEntity.ok(ApiResponse.success("UPI profile retrieved successfully", response));
    }

    // UPI ID Management
    @PostMapping("/id")
    @Operation(summary = "Create UPI ID", description = "Create new UPI ID")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "UpiId", description = "UPI ID created")
    public ResponseEntity<ApiResponse<UpiIdResponse>> createUpiId(@Valid @RequestBody CreateUpiIdRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiIdResponse response = upiService.createUpiId(request, email);
        return ResponseEntity.ok(ApiResponse.success("UPI ID created successfully", response));
    }

    @PutMapping("/id/{upiIdId}")
    @Operation(summary = "Update UPI ID", description = "Update UPI ID linked account")
    @RateLimited
    public ResponseEntity<ApiResponse<UpiIdResponse>> updateUpiId(
            @PathVariable Long upiIdId,
            @Valid @RequestBody UpdateUpiIdRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiIdResponse response = upiService.updateUpiId(upiIdId, request, email);
        return ResponseEntity.ok(ApiResponse.success("UPI ID updated successfully", response));
    }

    @DeleteMapping("/id/{upiIdId}")
    @Operation(summary = "Delete UPI ID", description = "Delete UPI ID")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> deleteUpiId(@PathVariable Long upiIdId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        upiService.deleteUpiId(upiIdId, email);
        return ResponseEntity.ok(ApiResponse.success("UPI ID deleted successfully", null));
    }

    @PostMapping("/id/{upiIdId}/set-primary")
    @Operation(summary = "Set primary UPI ID", description = "Set UPI ID as primary")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> setPrimaryUpiId(@PathVariable Long upiIdId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        upiService.setPrimaryUpiId(upiIdId, email);
        return ResponseEntity.ok(ApiResponse.success("Primary UPI ID set successfully", null));
    }

    @GetMapping("/id/verify/{upiId}")
    @Operation(summary = "Verify UPI ID", description = "Check if UPI ID exists")
    @RateLimited
    public ResponseEntity<ApiResponse<Boolean>> verifyUpiId(@PathVariable String upiId) {
        boolean exists = upiService.verifyUpiId(upiId);
        return ResponseEntity.ok(ApiResponse.success("UPI ID verification completed", exists));
    }

    // UPI PIN Management
    @PostMapping("/pin/create")
    @Operation(summary = "Create UPI PIN", description = "Create UPI PIN for transactions")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "UpiPin", description = "UPI PIN created")
    public ResponseEntity<ApiResponse<Void>> createUpiPin(@Valid @RequestBody CreateUpiPinRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        upiPinService.createUpiPin(request, email);
        return ResponseEntity.ok(ApiResponse.success("UPI PIN created successfully", null));
    }

    @PostMapping("/pin/change")
    @Operation(summary = "Change UPI PIN", description = "Change existing UPI PIN")
    @RateLimited
    @Audited(action = AuditAction.UPDATE, entityType = "UpiPin", description = "UPI PIN changed")
    public ResponseEntity<ApiResponse<Void>> changeUpiPin(@Valid @RequestBody ChangeUpiPinRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        upiPinService.changeUpiPin(request, email);
        return ResponseEntity.ok(ApiResponse.success("UPI PIN changed successfully", null));
    }

    @PostMapping("/pin/verify")
    @Operation(summary = "Verify UPI PIN", description = "Verify UPI PIN")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> verifyUpiPin(@Valid @RequestBody VerifyUpiPinRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        upiPinService.verifyUpiPin(request, email);
        return ResponseEntity.ok(ApiResponse.success("UPI PIN verified successfully", null));
    }

    // UPI Transactions
    @PostMapping("/send-money")
    @Operation(summary = "Send money via UPI", description = "Send money using UPI ID")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Transaction", description = "UPI transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> sendMoney(
            @Valid @RequestBody UpiSendMoneyRequest request,
            HttpServletRequest httpRequest) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse response = upiTransactionService.sendMoney(request, email, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Money sent successfully", response));
    }

    @PostMapping("/qr/pay")
    @Operation(summary = "Pay via QR code", description = "Pay merchant/person via QR code")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Transaction", description = "QR payment")
    public ResponseEntity<ApiResponse<TransactionResponse>> payViaQr(
            @Valid @RequestBody ScanQrRequest request,
            HttpServletRequest httpRequest) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse response = upiTransactionService.payViaQr(request, email, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment successful", response));
    }

    // UPI QR Code Management
    @PostMapping("/qr/generate")
    @Operation(summary = "Generate QR code", description = "Generate UPI QR code for receiving payments")
    @RateLimited
    public ResponseEntity<ApiResponse<UpiQrResponse>> generateQr(@Valid @RequestBody GenerateQrRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiQrResponse response = upiQrService.generateQrCode(request, email);
        return ResponseEntity.ok(ApiResponse.success("QR code generated successfully", response));
    }

    @GetMapping("/qr/{qrId}")
    @Operation(summary = "Get QR code", description = "Get QR code details")
    @RateLimited
    public ResponseEntity<ApiResponse<UpiQrResponse>> getQrCode(@PathVariable String qrId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiQrResponse response = upiQrService.getQrCode(qrId, email);
        return ResponseEntity.ok(ApiResponse.success("QR code retrieved successfully", response));
    }

    @GetMapping("/qr/my/{upiId}")
    @Operation(summary = "Get my QR codes", description = "Get all QR codes for UPI ID")
    @RateLimited
    public ResponseEntity<ApiResponse<List<UpiQrResponse>>> getMyQrCodes(@PathVariable String upiId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<UpiQrResponse> response = upiQrService.getMyQrCodes(upiId, email);
        return ResponseEntity.ok(ApiResponse.success("QR codes retrieved successfully", response));
    }

    @DeleteMapping("/qr/{qrId}")
    @Operation(summary = "Deactivate QR code", description = "Deactivate QR code")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> deactivateQr(@PathVariable String qrId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        upiQrService.deactivateQrCode(qrId, email);
        return ResponseEntity.ok(ApiResponse.success("QR code deactivated successfully", null));
    }

    // UPI Collect Requests
    @PostMapping("/collect-request")
    @Operation(summary = "Request money", description = "Request money from another UPI ID")
    @RateLimited
    public ResponseEntity<ApiResponse<UpiCollectRequestResponse>> requestMoney(
            @Valid @RequestBody UpiCollectMoneyRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        UpiCollectRequestResponse response = collectRequestService.createCollectRequest(request, email);
        return ResponseEntity.ok(ApiResponse.success("Money request sent successfully", response));
    }

    @GetMapping("/collect-request/pending/{payerUpiId}")
    @Operation(summary = "Get pending requests", description = "Get pending money requests")
    @RateLimited
    public ResponseEntity<ApiResponse<List<UpiCollectRequestResponse>>> getPendingRequests(@PathVariable String payerUpiId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<UpiCollectRequestResponse> response = collectRequestService.getPendingRequestsForPayer(payerUpiId, email);
        return ResponseEntity.ok(ApiResponse.success("Pending requests retrieved successfully", response));
    }

    @GetMapping("/collect-request/my/{requesterUpiId}")
    @Operation(summary = "Get my requests", description = "Get my money requests")
    @RateLimited
    public ResponseEntity<ApiResponse<List<UpiCollectRequestResponse>>> getMyRequests(@PathVariable String requesterUpiId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<UpiCollectRequestResponse> response = collectRequestService.getMyCollectRequests(requesterUpiId, email);
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", response));
    }

    @PostMapping("/collect-request/{requestId}/approve")
    @Operation(summary = "Approve money request", description = "Approve money request")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> approveRequest(@PathVariable String requestId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        collectRequestService.approveCollectRequest(requestId, email);
        return ResponseEntity.ok(ApiResponse.success("Request approved successfully", null));
    }

    @PostMapping("/collect-request/{requestId}/reject")
    @Operation(summary = "Reject money request", description = "Reject money request")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> rejectRequest(@PathVariable String requestId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        collectRequestService.rejectCollectRequest(requestId, email);
        return ResponseEntity.ok(ApiResponse.success("Request rejected successfully", null));
    }
}