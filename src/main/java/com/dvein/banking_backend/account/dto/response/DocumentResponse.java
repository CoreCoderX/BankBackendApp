package com.dvein.banking_backend.account.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dvein.banking_backend.common.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document response")
public class DocumentResponse {

    @Schema(description = "Document ID", example = "1")
    private Long documentId;

    @Schema(description = "Document type")
    private DocumentType documentType;

    @Schema(description = "Document number")
    private String documentNumber;

    @Schema(description = "Expiry date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Schema(description = "Issue date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    @Schema(description = "Issuing authority")
    private String issuingAuthority;

    @Schema(description = "Verified")
    private boolean verified;

    @Schema(description = "Expired")
    private boolean expired;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}