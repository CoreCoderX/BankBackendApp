package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.transaction.enums.PaymentMethod;
import com.dvein.banking_backend.transaction.enums.TransactionMode;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
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
@Schema(description = "Transaction response")
public class TransactionResponse {

    @Schema(description = "Transaction ID", example = "1")
    private Long id;

    @Schema(description = "Transaction reference ID", example = "TXN20240115123456")
    private String transactionId;

    @Schema(description = "Sender account number")
    private String senderAccountNumber;

    @Schema(description = "Receiver account number")
    private String receiverAccountNumber;

    @Schema(description = "Receiver name")
    private String receiverName;

    @Schema(description = "Receiver bank name")
    private String receiverBankName;

    @Schema(description = "Transaction amount", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "INR")
    private String currency;

    @Schema(description = "Transaction type")
    private TransactionType transactionType;

    @Schema(description = "Transaction mode")
    private TransactionMode transactionMode;

    @Schema(description = "Payment method")
    private PaymentMethod paymentMethod;

    @Schema(description = "Transaction status")
    private TransactionStatus status;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Remarks")
    private String remarks;

    @Schema(description = "Reference number")
    private String referenceNumber;

    @Schema(description = "UTR number")
    private String utrNumber;

    @Schema(description = "Transaction fee", example = "5.00")
    private BigDecimal transactionFee;

    @Schema(description = "GST", example = "0.90")
    private BigDecimal gst;

    @Schema(description = "Total amount (including fees)", example = "5005.90")
    private BigDecimal totalAmount;

    @Schema(description = "Sender balance before transaction")
    private BigDecimal senderBalanceBefore;

    @Schema(description = "Sender balance after transaction")
    private BigDecimal senderBalanceAfter;

    @Schema(description = "Initiated at")
    private LocalDateTime initiatedAt;

    @Schema(description = "Completed at")
    private LocalDateTime completedAt;

    @Schema(description = "Failed at")
    private LocalDateTime failedAt;

    @Schema(description = "Failure reason")
    private String failureReason;

    @Schema(description = "Is flagged for fraud", example = "false")
    private boolean flagged;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}