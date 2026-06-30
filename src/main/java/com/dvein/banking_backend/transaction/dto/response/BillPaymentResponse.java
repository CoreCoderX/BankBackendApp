package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bill payment response")
public class BillPaymentResponse {

    @Schema(description = "Transaction details")
    private TransactionResponse transaction;

    @Schema(description = "Biller name")
    private String billerName;

    @Schema(description = "Bill category")
    private String billCategory;

    @Schema(description = "Bill number")
    private String billNumber;

    @Schema(description = "Due date")
    private LocalDate dueDate;

    @Schema(description = "Late fee", example = "50.00")
    private BigDecimal lateFee;
}