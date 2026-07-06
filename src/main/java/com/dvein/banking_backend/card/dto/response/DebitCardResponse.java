package com.dvein.banking_backend.card.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dvein.banking_backend.common.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Debit card response")
public class DebitCardResponse {

    @Schema(description = "Card ID", example = "1")
    private Long cardId;

    @Schema(description = "Masked card number", example = "4532 **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Card holder name")
    private String cardHolderName;

    @Schema(description = "Expiry date")
    @JsonFormat(pattern = "MM/yy")
    private LocalDate expiryDate;

    @Schema(description = "Card status")
    private CardStatus status;

    @Schema(description = "International transaction enabled")
    private boolean internationalEnabled;

    @Schema(description = "Online transaction enabled")
    private boolean onlineTransactionEnabled;

    @Schema(description = "ATM withdrawal enabled")
    private boolean atmWithdrawalEnabled;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Activated at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activatedAt;
}