package com.dvein.banking_backend.loan.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEligibilityResponse {

    private Boolean isEligible;
    private BigDecimal maxEligibleAmount;
    private Integer creditScore;
    private List<String> eligibilityChecks;
    private List<String> failureReasons;
}