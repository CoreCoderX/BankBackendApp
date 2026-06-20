package com.dvein.banking_backend.loan.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutstandingBalanceResponse {

    private String loanNumber;
    private BigDecimal remainingPrincipal;
    private BigDecimal interestDue;
    private BigDecimal penaltyDue;
    private BigDecimal totalOutstanding;
    private Integer emisRemaining;
    private Integer emisPaid;
    private LocalDate nextEmiDate;
    private BigDecimal nextEmiAmount;
}