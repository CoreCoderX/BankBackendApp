package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.transaction.enums.UpiStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UPI collect request response")
public class UpiCollectRequestResponse {

    @Schema(description = "Request ID", example = "REQ20240115123456")
    private String requestId;

    @Schema(description = "Requester UPI ID", example = "john@dveinbank")
    private String requesterUpiId;

    @Schema(description = "Payer UPI ID", example = "jane@dveinbank")
    private String payerUpiId;

    @Schema(description = "Requested amount", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Status")
    private UpiStatus status;

    @Schema(description = "Expires at")
    private LocalDateTime expiresAt;

    @Schema(description = "Responded at")
    private LocalDateTime respondedAt;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Is expired", example = "false")
    private boolean expired;
}