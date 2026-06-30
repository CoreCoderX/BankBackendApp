package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction list response")
public class TransactionListResponse {

    @Schema(description = "Paginated transactions")
    private PageResponse<TransactionResponse> transactions;

    @Schema(description = "Total transaction count")
    private long totalCount;

    @Schema(description = "Completed transactions count")
    private long completedCount;

    @Schema(description = "Failed transactions count")
    private long failedCount;

    @Schema(description = "Pending transactions count")
    private long pendingCount;
}