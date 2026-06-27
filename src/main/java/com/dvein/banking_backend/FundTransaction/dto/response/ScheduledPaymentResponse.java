package com.dvein.banking_backend.FundTransaction.dto.response;

import com.dvein.banking_backend.FundTransaction.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPaymentResponse {

    private Long id;
    private String beneficiaryNickname;
    private String beneficiaryAccountNumber;
    private BigDecimal amount;
    private String frequency;
    private LocalDate nextExecutionDate;
    private TransactionStatus status;
    private String remarks;
    private LocalDateTime createdAt;
}