// report/dto/request/ReportFilterRequest.java
package com.dvein.banking_backend.report.dto.request;

import com.dvein.banking_backend.report.enums.ReportFilterType;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.common.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFilterRequest {

    private List<ReportFilterType> filterTypes;

    // Date filters
    private LocalDate startDate;
    private LocalDate endDate;

    // Transaction filters
    private TransactionType transactionType;
    private List<String> transactionStatuses;

    // Amount filters
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Account filters
    private AccountType accountType;
    private String accountNumber;
    private List<String> accountNumbers;

    // Customer filters
    private String customerId;
    private String customerStatus;
    private List<String> customerStatuses;

    // Merchant filters
    private String merchantId;
    private List<String> merchantIds;

    // Card filters
    private String cardNumber;
    private List<String> cardStatuses;

    // Loan filters
    private String loanId;
    private List<String> loanStatuses;

    // Generic search
    private String searchText;
}