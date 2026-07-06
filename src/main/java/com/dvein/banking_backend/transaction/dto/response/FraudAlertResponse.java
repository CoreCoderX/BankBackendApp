package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.transaction.enums.FraudRiskLevel;
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
@Schema(description = "Fraud alert response")
public class FraudAlertResponse {

    @Schema(description = "Alert ID", example = "1")
    private Long id;

    @Schema(description = "Transaction ID")
    private String transactionId;

    @Schema(description = "Rule triggered")
    private String ruleTriggered;

    @Schema(description = "Risk score", example = "75.5")
    private BigDecimal riskScore;

    @Schema(description = "Risk level")
    private FraudRiskLevel riskLevel;

    @Schema(description = "Details")
    private String details;

    @Schema(description = "Action taken")
    private String actionTaken;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}