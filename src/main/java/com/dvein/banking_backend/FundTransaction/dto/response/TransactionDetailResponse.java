package com.dvein.banking_backend.FundTransaction.dto.response;

import com.dvein.banking_backend.FundTransaction.enums.PaymentChannel;
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
public class TransactionDetailResponse {

    private Long id;
    private String transactionId;
    private String referenceNumber;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private PaymentChannel channel;
    private String remarks;
    private String senderAccountNumber;
    private String senderAccountHolderName;
    private String receiverAccountNumber;
    private String receiverAccountHolderName;
    private String ipAddress;
    private String deviceInfo;
    private Boolean isFlagged;
    private String flagReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}