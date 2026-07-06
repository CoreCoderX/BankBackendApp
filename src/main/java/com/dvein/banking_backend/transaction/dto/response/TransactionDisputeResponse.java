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
@Schema(description = "Transaction dispute response")
public class TransactionDisputeResponse {

    @Schema(description = "Dispute ID", example = "1")
    private Long id;

    @Schema(description = "Transaction ID")
    private String transactionId;

    @Schema(description = "Dispute reason")
    private String disputeReason;

    @Schema(description = "Status")
    private DisputeStatus status;

    @Schema(description = "Resolution")
    private String resolution;

    @Schema(description = "Resolved by")
    private String resolvedBy;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Resolved at")
    private LocalDateTime resolvedAt;

    @Schema(description = "Transaction details")
    private TransactionResponse transaction;
}