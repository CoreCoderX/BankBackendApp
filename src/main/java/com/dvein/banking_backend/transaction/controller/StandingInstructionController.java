package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.CreateStandingInstructionRequest;
import com.dvein.banking_backend.transaction.dto.response.StandingInstructionResponse;
import com.dvein.banking_backend.transaction.service.StandingInstructionService;
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
@RequestMapping("/standing-instructions")
@RequiredArgsConstructor
@Tag(name = "Standing Instructions", description = "Standing instruction endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class StandingInstructionController {

    private final StandingInstructionService siService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping
    @Operation(summary = "Create SI", description = "Create standing instruction")
    @RateLimited
    public ResponseEntity<ApiResponse<StandingInstructionResponse>> createSI(
            @Valid @RequestBody CreateStandingInstructionRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        StandingInstructionResponse response = siService.createSI(request, email);
        return ResponseEntity.ok(ApiResponse.success("Standing instruction created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get SIs", description = "Get all standing instructions")
    @RateLimited
    public ResponseEntity<ApiResponse<List<StandingInstructionResponse>>> getSIs() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<StandingInstructionResponse> response = siService.getMySIs(email);
        return ResponseEntity.ok(ApiResponse.success("Standing instructions retrieved successfully", response));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause SI", description = "Pause standing instruction")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> pauseSI(@PathVariable Long id) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        siService.pauseSI(id, email);
        return ResponseEntity.ok(ApiResponse.success("Standing instruction paused successfully", null));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume SI", description = "Resume standing instruction")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> resumeSI(@PathVariable Long id) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        siService.resumeSI(id, email);
        return ResponseEntity.ok(ApiResponse.success("Standing instruction resumed successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SI", description = "Delete standing instruction")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> deleteSI(@PathVariable Long id) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        siService.deleteSI(id, email);
        return ResponseEntity.ok(ApiResponse.success("Standing instruction deleted successfully", null));
    }
}