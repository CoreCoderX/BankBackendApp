package com.dvein.banking_backend.auth.dto.request;

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
@Schema(description = "Create MPIN request")
public class CreateMpinRequest {

    @NotBlank(message = "MPIN is required")
    @Pattern(regexp = "^\\d{4}$", message = "MPIN must be 4 digits")
    @Schema(description = "4-digit MPIN", example = "1234")
    private String mpin;

    @NotBlank(message = "Confirm MPIN is required")
    @Schema(description = "Confirm MPIN", example = "1234")
    private String confirmMpin;
}