package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bank response")
public class BankResponse {

    @Schema(description = "Bank ID", example = "1")
    private Long id;

    @Schema(description = "Bank code", example = "HDFC")
    private String bankCode;

    @Schema(description = "Bank name", example = "HDFC Bank")
    private String bankName;

    @Schema(description = "IFSC prefix", example = "HDFC")
    private String ifscPrefix;

    @Schema(description = "Active", example = "true")
    private boolean active;
}