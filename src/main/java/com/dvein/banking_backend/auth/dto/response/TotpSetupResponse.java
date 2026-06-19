package com.dvein.banking_backend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "TOTP setup response")
public class TotpSetupResponse {

    @Schema(description = "Secret key for manual entry")
    private String secret;

    @Schema(description = "QR code as base64 image")
    private String qrCode;

    @Schema(description = "Issuer name", example = "DVein Bank")
    private String issuer;

    @Schema(description = "Account name (user email)", example = "john.doe@example.com")
    private String accountName;
}