package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.SchedulePaymentRequest;
import com.dvein.banking_backend.transaction.dto.response.ScheduledPaymentResponse;
import com.dvein.banking_backend.transaction.service.ScheduledPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scheduled-payments")
@RequiredArgsConstructor
@Tag(name = "Scheduled Payments", description = "Scheduled payment endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class ScheduledPaymentController {

    private final ScheduledPaymentService scheduledPaymentService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping
    @Operation(summary = "Create scheduled payment", description = "Create new scheduled payment")
    @RateLimited
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> createScheduledPayment(
            @Valid @RequestBody SchedulePaymentRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        ScheduledPaymentResponse response = scheduledPaymentService.createScheduledPayment(request, email);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get scheduled payments", description = "Get all scheduled payments")
    @RateLimited
    public ResponseEntity<ApiResponse<List<ScheduledPaymentResponse>>> getScheduledPayments() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<ScheduledPaymentResponse> response = scheduledPaymentService.getMyScheduledPayments(email);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payments retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update scheduled payment", description = "Update scheduled payment")
    @RateLimited
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> updateScheduledPayment(
            @PathVariable Long id,
            @Valid @RequestBody SchedulePaymentRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        ScheduledPaymentResponse response = scheduledPaymentService.updateScheduledPayment(id, request, email);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment updated successfully", response));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause scheduled payment", description = "Pause scheduled payment")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> pauseScheduledPayment(@PathVariable Long id) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        scheduledPaymentService.pauseScheduledPayment(id, email);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment paused successfully", null));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume scheduled payment", description = "Resume scheduled payment")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> resumeScheduledPayment(@PathVariable Long id) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        scheduledPaymentService.resumeScheduledPayment(id, email);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment resumed successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel scheduled payment", description = "Cancel scheduled payment")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> cancelScheduledPayment(@PathVariable Long id) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        scheduledPaymentService.cancelScheduledPayment(id, email);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment cancelled successfully", null));
    }
}