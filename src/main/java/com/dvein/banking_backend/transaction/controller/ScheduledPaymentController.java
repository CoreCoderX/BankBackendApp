package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.transaction.dto.request.ScheduledPaymentRequest;
import com.dvein.banking_backend.transaction.dto.response.ScheduledPaymentResponse;
import com.dvein.banking_backend.transaction.service.ScheduledPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ScheduledPaymentController {

    private final ScheduledPaymentService scheduledPaymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> createScheduledPayment(
            @Valid @RequestBody ScheduledPaymentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scheduledPaymentService.createScheduledPayment(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduledPaymentResponse>>> getAllScheduledPayments() {
        return ResponseEntity.ok(scheduledPaymentService.getAllScheduledPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> getScheduledPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledPaymentService.getScheduledPaymentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> updateScheduledPayment(
            @PathVariable Long id,
            @Valid @RequestBody ScheduledPaymentRequest request
    ) {
        return ResponseEntity.ok(scheduledPaymentService.updateScheduledPayment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScheduledPayment(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledPaymentService.deleteScheduledPayment(id));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<Void>> pauseScheduledPayment(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledPaymentService.pauseScheduledPayment(id));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeScheduledPayment(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledPaymentService.resumeScheduledPayment(id));
    }
}