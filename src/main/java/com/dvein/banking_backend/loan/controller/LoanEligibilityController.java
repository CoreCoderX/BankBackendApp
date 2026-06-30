package com.dvein.banking_backend.loan.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.loan.dto.request.EmiCalculatorRequest;
import com.dvein.banking_backend.loan.dto.response.EmiCalculationResponse;
import com.dvein.banking_backend.loan.dto.response.LoanEligibilityResponse;
import com.dvein.banking_backend.loan.service.EmiCalculatorService;
import com.dvein.banking_backend.loan.service.LoanEligibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class LoanEligibilityController {

    private final LoanEligibilityService loanEligibilityService;
    private final EmiCalculatorService emiCalculatorService;

    @GetMapping("/eligibility/{accountId}")
    public ResponseEntity<ApiResponse<LoanEligibilityResponse>> checkEligibility(
            @PathVariable Long accountId
    ) {
        return ResponseEntity.ok(loanEligibilityService.checkEligibility(accountId));
    }

    @PostMapping("/calculate-emi")
    public ResponseEntity<ApiResponse<EmiCalculationResponse>> calculateEmi(
            @Valid @RequestBody EmiCalculatorRequest request
    ) {
        return ResponseEntity.ok(emiCalculatorService.calculateEmiWithSchedule(request));
    }
}