package com.dvein.banking_backend.card.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Set card PIN request")
public class SetCardPinRequest {

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be 4 digits")
    @Schema(description = "4-digit PIN", example = "1234")
    private String pin;

    @NotBlank(message = "Confirm PIN is required")
    @Schema(description = "Confirm PIN", example = "1234")
    private String confirmPin;
}