package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.loan.dto.request.EmiCalculatorRequest;
import com.dvein.banking_backend.loan.dto.response.EmiCalculationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EmiCalculatorService {

    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * EMI Formula:
     * EMI = P × R × (1+R)^N / ((1+R)^N - 1)
     * Where:
     * P = Principal
     * R = Monthly Interest Rate (annual/100/12)
     * N = Tenure in months
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, Integer tenureMonths) {
        // Monthly interest rate: (annual% / 100) / 12
        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(100), 10, ROUNDING_MODE)
                .divide(BigDecimal.valueOf(12), 10, ROUNDING_MODE);

        log.debug("Principal: {}, Annual Rate: {}%, Monthly Rate: {}", principal, annualInterestRate, monthlyRate);

        // If interest rate is 0
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), DECIMAL_SCALE, ROUNDING_MODE);
        }

        // (1+R)^N
        BigDecimal onePlusRPowerN = onePlusRatePower(monthlyRate, tenureMonths);

        // EMI = P × R × (1+R)^N / ((1+R)^N - 1)
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

        BigDecimal emi = numerator.divide(denominator, DECIMAL_SCALE, ROUNDING_MODE);
        log.debug("Calculated EMI: {}", emi);

        return emi;
    }

    private BigDecimal onePlusRatePower(BigDecimal monthlyRate, Integer tenureMonths) {
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal result = BigDecimal.ONE;

        for (int i = 0; i < tenureMonths; i++) {
            result = result.multiply(onePlusR);
        }

        return result;
    }

    public ApiResponse<EmiCalculationResponse> calculateEmiWithSchedule(EmiCalculatorRequest request) {
        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal annualRate = request.getInterestRate();
        Integer tenure = request.getTenureMonths();

        BigDecimal emi = calculateEmi(principal, annualRate, tenure);

        BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(tenure));
        BigDecimal totalInterest = totalPayable.subtract(principal);

        // Generate amortization schedule
        List<EmiCalculationResponse.AmortizationEntry> schedule =
                generateAmortizationSchedule(principal, annualRate, tenure, emi);

        EmiCalculationResponse response = EmiCalculationResponse.builder()
                .emiAmount(emi)
                .totalInterest(totalInterest)
                .totalPayable(totalPayable)
                .principalAmount(principal)
                .interestRate(annualRate)
                .tenureMonths(tenure)
                .amortizationSchedule(schedule)
                .build();

        return ApiResponse.success("EMI calculated successfully", response);
    }

    public List<EmiCalculationResponse.AmortizationEntry> generateAmortizationSchedule(
            BigDecimal principal,
            BigDecimal annualRate,
            Integer tenure,
            BigDecimal emi
    ) {
        List<EmiCalculationResponse.AmortizationEntry> schedule = new ArrayList<>();

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, ROUNDING_MODE)
                .divide(BigDecimal.valueOf(12), 10, ROUNDING_MODE);

        BigDecimal outstandingBalance = principal;

        for (int i = 1; i <= tenure; i++) {
            // Interest component = outstanding × monthly rate
            BigDecimal interestComponent = outstandingBalance
                    .multiply(monthlyRate)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE);

            // Principal component = EMI - interest
            BigDecimal principalComponent = emi.subtract(interestComponent)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE);

            BigDecimal currentEmi = emi;

            // For last EMI, adjust to clear remaining balance
            if (i == tenure) {
                principalComponent = outstandingBalance.setScale(DECIMAL_SCALE, ROUNDING_MODE);
                currentEmi = interestComponent.add(principalComponent);
            }

            // Update outstanding balance
            outstandingBalance = outstandingBalance
                    .subtract(principalComponent)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE);

            if (outstandingBalance.compareTo(BigDecimal.ZERO) < 0) {
                outstandingBalance = BigDecimal.ZERO;
            }

            EmiCalculationResponse.AmortizationEntry entry =
                    EmiCalculationResponse.AmortizationEntry.builder()
                            .emiNumber(i)
                            .emiAmount(currentEmi)
                            .principalComponent(principalComponent)
                            .interestComponent(interestComponent)
                            .outstandingBalance(outstandingBalance)
                            .build();

            schedule.add(entry);

            log.debug("EMI {}: Interest={}, Principal={}, Balance={}",
                    i, interestComponent, principalComponent, outstandingBalance);
        }

        return schedule;
    }
}