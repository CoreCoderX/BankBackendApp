package com.dvein.banking_backend.FundTransaction.dto.response;

import com.dvein.banking_backend.FundTransaction.enums.TransactionStatus;
import com.dvein.banking_backend.FundTransaction.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryResponse {

    private String transactionId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private LocalDateTime transactionDate;
}