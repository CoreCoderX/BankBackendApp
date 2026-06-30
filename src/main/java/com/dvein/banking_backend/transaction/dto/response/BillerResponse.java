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
@Schema(description = "Saved biller response")
public class BillerResponse {

    @Schema(description = "Biller ID", example = "1")
    private Long id;

    @Schema(description = "Biller name", example = "Tata Power")
    private String billerName;

    @Schema(description = "Biller category", example = "ELECTRICITY")
    private String billerCategory;

    @Schema(description = "Account number")
    private String accountNumber;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Auto-pay enabled", example = "false")
    private boolean autoPayEnabled;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}