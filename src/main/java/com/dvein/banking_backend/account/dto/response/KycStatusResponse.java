package com.dvein.banking_backend.account.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dvein.banking_backend.common.enums.KycStatus;
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
@Schema(description = "KYC status response")
public class KycStatusResponse {

    @Schema(description = "KYC ID", example = "1")
    private Long kycId;

    @Schema(description = "KYC status")
    private KycStatus status;

    @Schema(description = "Rejection reason")
    private String rejectionReason;

    @Schema(description = "Submitted at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    @Schema(description = "Approved at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Schema(description = "Approved by")
    private String approvedBy;

    @Schema(description = "Expiry date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Schema(description = "Expired")
    private boolean expired;
}