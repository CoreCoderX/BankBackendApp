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
public class TransactionResponse {

    private Long id;
    private String transactionId;
    private String referenceNumber;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String remarks;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private LocalDateTime createdAt;
}