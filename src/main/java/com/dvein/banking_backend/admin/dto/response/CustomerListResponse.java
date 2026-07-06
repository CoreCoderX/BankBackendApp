package com.dvein.banking_backend.admin.dto.response;

import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
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
@Schema(description = "Customer list response")
public class CustomerListResponse {

    @Schema(description = "Paginated customer data")
    private PageResponse<CustomerProfileResponse> customers;

    @Schema(description = "Total count", example = "1000")
    private long totalCount;

    @Schema(description = "Active count", example = "950")
    private long activeCount;

    @Schema(description = "Blocked count", example = "50")
    private long blockedCount;
}