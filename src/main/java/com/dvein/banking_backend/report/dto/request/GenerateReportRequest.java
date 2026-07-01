// report/dto/request/GenerateReportRequest.java
package com.dvein.banking_backend.report.dto.request;

import com.dvein.banking_backend.report.enums.ReportFormat;
import com.dvein.banking_backend.report.enums.ReportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateReportRequest {

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotNull(message = "Report format is required")
    private ReportFormat format;

    private LocalDate startDate;
    private LocalDate endDate;

    @Valid
    private ReportFilterRequest filters;

    private String notes;
}