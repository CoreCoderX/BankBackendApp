package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.transaction.dto.request.AddBeneficiaryRequest;
import com.dvein.banking_backend.transaction.dto.response.BeneficiaryResponse;
import com.dvein.banking_backend.transaction.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @Valid @RequestBody AddBeneficiaryRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(beneficiaryService.addBeneficiary(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getAllBeneficiaries() {
        return ResponseEntity.ok(beneficiaryService.getAllBeneficiaries());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(@PathVariable Long id) {
        return ResponseEntity.ok(beneficiaryService.deleteBeneficiary(id));
    }
}