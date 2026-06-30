package com.dvein.banking_backend.reports.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatementDataDTO {

    private Long                     accountId;
    private LocalDate                fromDate;
    private LocalDate                toDate;
    private List<Map<String, Object>> transactions;
    private Long                     totalTransactions;
    private BigDecimal               totalCreditAmount;
    private BigDecimal               totalDebitAmount;
}