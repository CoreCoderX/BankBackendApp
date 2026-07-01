// report/dto/response/ReportResponse.java
package com.dvein.banking_backend.report.dto.response;

import com.dvein.banking_backend.report.enums.ReportFormat;
import com.dvein.banking_backend.report.enums.ReportStatus;
import com.dvein.banking_backend.report.enums.ReportType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private Long id;
    private ReportType reportType;
    private ReportFormat format;
    private ReportStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String downloadUrl;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private Integer downloadCount;
    private Long fileSizeInBytes;
    private String mimeType;
    private LocalDateTime createdAt;
    private String errorMessage;
}