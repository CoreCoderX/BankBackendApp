package com.dvein.banking_backend.card.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dvein.banking_backend.common.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credit card response")
public class CreditCardResponse {

    @Schema(description = "Card ID", example = "1")
    private Long cardId;

    @Schema(description = "Masked card number", example = "4532 **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Card holder name")
    private String cardHolderName;

    @Schema(description = "Expiry date")
    @JsonFormat(pattern = "MM/yy")
    private LocalDate expiryDate;

    @Schema(description = "Credit limit")
    private BigDecimal creditLimit;

    @Schema(description = "Available credit")
    private BigDecimal availableCredit;

    @Schema(description = "Outstanding balance")
    private BigDecimal outstandingBalance;

    @Schema(description = "Interest rate")
    private BigDecimal interestRate;

    @Schema(description = "Card status")
    private CardStatus status;

    @Schema(description = "Approved")
    private boolean approved;

    @Schema(description = "Rejection reason")
    private String rejectionReason;

    @Schema(description = "Billing due date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billingDueDate;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Approved at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Schema(description = "Activated at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activatedAt;
}