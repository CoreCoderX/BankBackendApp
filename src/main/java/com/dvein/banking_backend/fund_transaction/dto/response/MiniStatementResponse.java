package com.dvein.banking_backend.transaction.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiniStatementResponse {

    private List<TransactionResponse> transactions;
    private Integer totalTransactions;
    private LocalDateTime generatedAt;
}