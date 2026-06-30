package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.transaction.enums.DisputeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed dispute response with full transaction info")
public class TransactionDisputeDetailResponse {

    @Schema(description = "Dispute ID")
    private Long id;

    @Schema(description = "Transaction ID")
    private String transactionId;

    @Schema(description = "Transaction amount")
    private java.math.BigDecimal amount;

    @Schema(description = "Transaction type")
    private String transactionType;

    @Schema(description = "Transaction status")
    private String transactionStatus;

    @Schema(description = "Dispute reason")
    private String disputeReason;

    @Schema(description = "Dispute status")
    private DisputeStatus status;

    @Schema(description = "Resolution")
    private String resolution;

    @Schema(description = "Resolved by")
    private String resolvedBy;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Resolved at")
    private LocalDateTime resolvedAt;
}