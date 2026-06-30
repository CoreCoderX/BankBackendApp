package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction search request")
public class TransactionSearchRequest {

    @Schema(description = "Account ID")
    private Long accountId;

    @Schema(description = "Transaction ID")
    private String transactionId;

    @Schema(description = "Reference number")
    private String referenceNumber;

    @Schema(description = "Transaction type")
    private String transactionType;

    @Schema(description = "Transaction status")
    private String status;

    @Schema(description = "Payment method")
    private String paymentMethod;

    @Schema(description = "Minimum amount")
    private BigDecimal minAmount;

    @Schema(description = "Maximum amount")
    private BigDecimal maxAmount;

    @Schema(description = "Start date")
    private LocalDate startDate;

    @Schema(description = "End date")
    private LocalDate endDate;

    @Schema(description = "Beneficiary account number")
    private String beneficiaryAccountNumber;

    @Schema(description = "Page number", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "20")
    private Integer size;

    @Schema(description = "Sort by field", example = "initiatedAt")
    private String sortBy;

    @Schema(description = "Sort direction", example = "DESC")
    private String sortDirection;
}