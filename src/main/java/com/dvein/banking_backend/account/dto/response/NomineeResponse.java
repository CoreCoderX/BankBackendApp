package com.dvein.banking_backend.account.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nominee response")
public class NomineeResponse {

    @Schema(description = "Nominee ID", example = "1")
    private Long nomineeId;

    @Schema(description = "Nominee name")
    private String nomineeName;

    @Schema(description = "Date of birth")
    private String nomineeDateOfBirth;

    @Schema(description = "Relationship")
    private String nomineeRelationship;

    @Schema(description = "Phone")
    private String nomineePhone;

    @Schema(description = "Email")
    private String nomineeEmail;

    @Schema(description = "Address")
    private String nomineeAddress;

    @Schema(description = "Percentage")
    private BigDecimal percentage;

    @Schema(description = "Active")
    private boolean active;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}