package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UPI profile response")
public class UpiProfileResponse {

    @Schema(description = "Profile ID", example = "1")
    private Long id;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Primary UPI ID")
    private String primaryUpiId;

    @Schema(description = "All UPI IDs")
    private List<UpiIdResponse> upiIds;

    @Schema(description = "PIN set", example = "true")
    private boolean pinSet;

    @Schema(description = "Is active", example = "true")
    private boolean active;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}