package com.dvein.banking_backend.admin.dto.request;

import com.dvein.banking_backend.common.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update customer status request")
public class UpdateCustomerStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New status", example = "BLOCKED")
    private CustomerStatus status;

    @Schema(description = "Reason for status change", example = "Suspicious activity detected")
    private String reason;
}