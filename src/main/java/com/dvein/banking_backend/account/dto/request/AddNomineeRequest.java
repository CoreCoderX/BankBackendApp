package com.dvein.banking_backend.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Add nominee request")
public class AddNomineeRequest {

    @NotBlank(message = "Nominee name is required")
    @Schema(description = "Nominee name", example = "Jane Doe")
    private String nomineeName;

    @Schema(description = "Date of birth", example = "1995-05-20")
    private String nomineeDateOfBirth;

    @NotBlank(message = "Relationship is required")
    @Schema(description = "Relationship", example = "Sister")
    private String nomineeRelationship;

    @Schema(description = "Phone number", example = "9876543210")
    private String nomineePhone;

    @Schema(description = "Email", example = "jane@example.com")
    private String nomineeEmail;

    @Schema(description = "Address")
    private String nomineeAddress;

    @NotNull(message = "Percentage is required")
    @Schema(description = "Percentage allocation", example = "100")
    private BigDecimal percentage;
}