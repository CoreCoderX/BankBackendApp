// report/controller/AdminReportController.java
package com.dvein.banking_backend.report.controller;

import com.dvein.banking_backend.report.dto.request.GenerateReportRequest;
import com.dvein.banking_backend.report.dto.response.ReportResponse;
import com.dvein.banking_backend.report.dto.response.ReportStatusResponse;
import com.dvein.banking_backend.report.service.ReportService;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Reports", description = "Admin report management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new admin report")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @Valid @RequestBody GenerateReportRequest request) {

        String adminId = String.valueOf(securityContextHelper.getCurrentUserId());
        ReportResponse report = reportService.generateAdminReport(adminId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report generation started", report));
    }

    @GetMapping
    @Operation(summary = "Get all admin reports")
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse>>> getAllReports(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> reports = reportService.getAdminReports(pageable);

        PageResponse<ReportResponse> pageResponse = PageResponse.of(reports);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", pageResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report details")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(
            @PathVariable Long id) {

        ReportResponse report = reportService.getAdminReportById(String.valueOf(id));
        return ResponseEntity.ok(ApiResponse.success("Report retrieved successfully", report));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get report generation status")
    public ResponseEntity<ApiResponse<ReportStatusResponse>> getReportStatus(
            @PathVariable Long id) {

        ReportStatusResponse status = reportService.getReportStatus(String.valueOf(id));
        return ResponseEntity.ok(ApiResponse.success("Report status retrieved", status));
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download report file")
    public ResponseEntity<?> downloadReport(
            @PathVariable Long id) {

        String adminId = String.valueOf(securityContextHelper.getCurrentUserId());
        byte[] fileData = reportService.downloadReport(String.valueOf(id), adminId,
                securityContextHelper.getUserRole());

        ReportResponse report = reportService.getAdminReportById(String.valueOf(id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + id + "." + report.getFormat().toString().toLowerCase() + "\"")
                .contentType(MediaType.parseMediaType(report.getMimeType()))
                .body(fileData);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a report")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long id) {

        reportService.deleteAdminReport(String.valueOf(id));
        return ResponseEntity.ok(ApiResponse.success("Report deleted successfully"));
    }
}