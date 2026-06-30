package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UPI ID response")
public class UpiIdResponse {

    @Schema(description = "UPI ID database ID", example = "1")
    private Long id;

    @Schema(description = "UPI ID", example = "john@dveinbank")
    private String upiId;

    @Schema(description = "Linked account number")
    private String linkedAccountNumber;

    @Schema(description = "Is primary", example = "true")
    private boolean primary;

    @Schema(description = "Is active", example = "true")
    private boolean active;

    @Schema(description = "Verified", example = "true")
    private boolean verified;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}