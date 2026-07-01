// report/dto/response/ReportStatusResponse.java
package com.dvein.banking_backend.report.dto.response;

import com.dvein.banking_backend.report.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStatusResponse {

    private Long reportId;
    private ReportStatus status;
    private Integer progressPercentage;
    private String message;
    private LocalDateTime estimatedCompletionTime;
    private String errorMessage;
}