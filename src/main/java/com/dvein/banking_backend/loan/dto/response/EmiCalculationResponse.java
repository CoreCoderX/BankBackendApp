package com.dvein.banking_backend.loan.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiCalculationResponse {

    private BigDecimal emiAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalPayable;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private List<AmortizationEntry> amortizationSchedule;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AmortizationEntry {
        private Integer emiNumber;
        private BigDecimal emiAmount;
        private BigDecimal principalComponent;
        private BigDecimal interestComponent;
        private BigDecimal outstandingBalance;
    }
}