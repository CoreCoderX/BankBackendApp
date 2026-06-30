package com.dvein.banking_backend.account.dto.request;

import com.dvein.banking_backend.common.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upload document request")
public class UploadDocumentRequest {

    @NotNull(message = "Document type is required")
    @Schema(description = "Document type", example = "AADHAAR")
    private DocumentType documentType;

    @Schema(description = "Document number", example = "123456789012")
    private String documentNumber;

    @Schema(description = "Expiry date", example = "2030-12-31")
    private String expiryDate;

    @Schema(description = "Issue date", example = "2020-12-31")
    private String issueDate;

    @Schema(description = "Issuing authority", example = "UIDAI")
    private String issuingAuthority;
}