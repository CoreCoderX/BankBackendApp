package com.dvein.banking_backend.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Consent request")
public class ConsentRequest {

    @NotBlank(message = "Consent type is required")
    @Schema(description = "Consent type", example = "TERMS_AND_CONDITIONS")
    private String consentType;

    @NotNull(message = "Acceptance is required")
    @Schema(description = "Accept consent", example = "true")
    private Boolean accept;
}