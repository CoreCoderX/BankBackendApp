package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.transaction.enums.ScheduleFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standing instruction response")
public class StandingInstructionResponse {

    @Schema(description = "SI ID", example = "1")
    private Long id;

    @Schema(description = "Sender account number")
    private String senderAccountNumber;

    @Schema(description = "Receiver account number")
    private String receiverAccountNumber;

    @Schema(description = "Receiver name")
    private String receiverName;

    @Schema(description = "Max amount per execution", example = "10000.00")
    private BigDecimal maxAmount;

    @Schema(description = "Transaction type")
    private String transactionType;

    @Schema(description = "Payment method")
    private String paymentMethod;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Frequency")
    private ScheduleFrequency frequency;

    @Schema(description = "Start date")
    private LocalDate startDate;

    @Schema(description = "End date")
    private LocalDate endDate;

    @Schema(description = "Next execution date")
    private LocalDate nextExecutionDate;

    @Schema(description = "Execution time")
    private LocalTime executionTime;

    @Schema(description = "Is active", example = "true")
    private boolean active;

    @Schema(description = "Is paused", example = "false")
    private boolean paused;

    @Schema(description = "Total executions", example = "10")
    private int totalExecutions;

    @Schema(description = "Successful executions", example = "9")
    private int successfulExecutions;

    @Schema(description = "Failed executions", example = "1")
    private int failedExecutions;

    @Schema(description = "Last executed at")
    private LocalDateTime lastExecutedAt;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}