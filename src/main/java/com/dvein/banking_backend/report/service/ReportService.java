// report/service/ReportService.java
package com.dvein.banking_backend.report.service;

import com.dvein.banking_backend.report.dto.request.GenerateReportRequest;
import com.dvein.banking_backend.report.dto.response.ReportResponse;
import com.dvein.banking_backend.report.dto.response.ReportStatusResponse;
import com.dvein.banking_backend.report.enums.ReportFormat;
import com.dvein.banking_backend.report.enums.ReportStatus;
import com.dvein.banking_backend.report.enums.ReportType;
import com.dvein.banking_backend.report.model.ReportFile;
import com.dvein.banking_backend.report.model.ReportRequest;
import com.dvein.banking_backend.report.repository.ReportRequestRepository;
import com.dvein.banking_backend.report.repository.ReportFileRepository;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportService {

    private final ReportRequestRepository reportRequestRepository;
    private final ReportFileRepository reportFileRepository;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;
    private final CsvReportService csvReportService;
    private final ObjectMapper objectMapper;

    @Value("${report.storage.path:reports}")
    private String reportStoragePath;

    @Value("${report.expiry.days:7}")
    private int reportExpiryDays;

    @Value("${report.max.size:104857600}") // 100MB in bytes
    private long maxReportSize;

    /**
     * Generate report request for customer
     */
    public ReportResponse generateCustomerReport(
            String customerId,
            GenerateReportRequest request) {

        log.info("Generating report for customer: {}, Type: {}", customerId, request.getReportType());

        ReportRequest reportRequest = ReportRequest.builder()
                .reportType(request.getReportType())
                .format(request.getFormat())
                .status(ReportStatus.PENDING)
                .requestedBy(customerId)
                .userRole(UserRole.CUSTOMER)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .filters(serializeFilters(request.getFilters()))
                .expiresAt(LocalDateTime.now().plusDays(reportExpiryDays))
                .downloadCount(0)
                .createdBy(customerId)
                .build();

        ReportRequest savedRequest = reportRequestRepository.save(reportRequest);

        // Trigger async report generation (pass only ID, not entity)
        generateReportAsync(String.valueOf(savedRequest.getId()));

        return mapToResponse(savedRequest, null);
    }

    /**
     * Generate report request for admin
     */
    public ReportResponse generateAdminReport(
            String adminId,
            GenerateReportRequest request) {

        log.info("Generating admin report: {}, Type: {}", adminId, request.getReportType());

        ReportRequest reportRequest = ReportRequest.builder()
                .reportType(request.getReportType())
                .format(request.getFormat())
                .status(ReportStatus.PENDING)
                .requestedBy(adminId)
                .userRole(UserRole.ADMIN)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .filters(serializeFilters(request.getFilters()))
                .expiresAt(LocalDateTime.now().plusDays(reportExpiryDays))
                .downloadCount(0)
                .createdBy(adminId)
                .build();

        ReportRequest savedRequest = reportRequestRepository.save(reportRequest);

        // Trigger async report generation (pass only ID, not entity)
        generateReportAsync(String.valueOf(savedRequest.getId()));

        return mapToResponse(savedRequest, null);
    }

    /**
     * Async report generation using Spring @Async
     * This ensures proper transaction management and avoids detached entity issues
     *
     * @param reportId The ID (UUID String) of the report to generate
     */
    @Async("reportTaskExecutor")
    @Transactional
    public void generateReportAsync(String reportId) {

        log.debug("Starting async report generation for ID: {}", reportId);

        try {
            // ✅ FIX 1: Reload entity from database in this thread's transaction context
            ReportRequest reportRequest = reportRequestRepository.findById(Long.valueOf(reportId))
                    .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

            // Update status to PROCESSING
            reportRequest.setStatus(ReportStatus.PROCESSING);
            reportRequestRepository.save(reportRequest);
            log.info("Report {} status updated to PROCESSING", reportId);

            // Generate report data
            // Set generation time before creating the report
            reportRequest.setGeneratedAt(LocalDateTime.now());
            reportRequestRepository.save(reportRequest);

// Generate report data
            byte[] reportData = generateReportData(reportRequest);

            // Validate file size
            if (reportData.length > maxReportSize) {
                log.warn("Report {} exceeds maximum size limit: {} bytes", reportId, reportData.length);
                reportRequest.setStatus(ReportStatus.FAILED);
                reportRequest.setErrorMessage("Report size exceeds maximum limit of " + (maxReportSize / (1024 * 1024)) + "MB");
                reportRequestRepository.save(reportRequest);
                return;
            }

            // Save file to disk
            String filePath = saveReportFile(reportRequest, reportData);
            log.debug("Report {} saved to: {}", reportId, filePath);

            // Create report file record
            ReportFile reportFile = ReportFile.builder()
                    .reportRequestId(reportRequest.getId())
                    .fileName(generateFileName(reportRequest))
                    .fileSize((long) reportData.length)
                    .mimeType(getMimeType(reportRequest.getFormat()))
                    .storagePath(filePath)
                    .downloadCount(0)
                    .build();

            reportFileRepository.save(reportFile);
            log.debug("Report file record created for: {}", reportId);

            // ✅ FIX 3: Generate correct download URL based on user role
            String downloadUrl = generateDownloadUrl(reportRequest);

            // Update report status to COMPLETED
            reportRequest.setStatus(ReportStatus.COMPLETED);
            reportRequest.setGeneratedAt(LocalDateTime.now());
            reportRequest.setFilePath(filePath);
            reportRequest.setDownloadUrl(downloadUrl);
            reportRequestRepository.save(reportRequest);

            log.info("Report {} generated successfully. Size: {} bytes, Path: {}",
                    reportId, reportData.length, filePath);

        } catch (Exception e) {
            log.error("Error generating report: {}", reportId, e);

            try {
                // Reload entity and update with error status
                ReportRequest reportRequest = reportRequestRepository.findById(Long.valueOf(reportId))
                        .orElse(null);

                if (reportRequest != null) {
                    reportRequest.setStatus(ReportStatus.FAILED);
                    reportRequest.setErrorMessage("Report generation failed: " + e.getMessage());
                    reportRequestRepository.save(reportRequest);
                    log.error("Report {} marked as FAILED", reportId);
                }
            } catch (Exception errorHandlingException) {
                log.error("Error while handling report generation error for ID: {}", reportId, errorHandlingException);
            }
        }
    }

    /**
     * Generate report data based on format
     */
    private byte[] generateReportData(ReportRequest reportRequest) throws IOException {
        log.debug("Generating {} report in {} format", reportRequest.getReportType(), reportRequest.getFormat());

        return switch (reportRequest.getFormat()) {
            case PDF -> pdfReportService.generatePdf(reportRequest);
            case EXCEL -> excelReportService.generateExcel(reportRequest);
            case CSV -> csvReportService.generateCsv(reportRequest);
        };
    }

    /**
     * Save report file to storage
     */
    private String saveReportFile(ReportRequest reportRequest, byte[] data) throws IOException {
        // Create directory structure: reports/role/year/
        Path directory = Paths.get(reportStoragePath,
                reportRequest.getUserRole().toString().toLowerCase(),
                String.valueOf(reportRequest.getCreatedAt().getYear()));

        Files.createDirectories(directory);
        log.debug("Created report directory: {}", directory);

        String fileName = generateFileName(reportRequest);
        Path filePath = directory.resolve(fileName);

        // Write file
        Files.write(filePath, data);
        log.debug("Report file written to: {}", filePath);

        return filePath.toString();
    }

    /**
     * Get customer reports with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReportResponse> getCustomerReports(String customerId, Pageable pageable) {
        Page<ReportRequest> reports = reportRequestRepository
                .findByRequestedByOrderByCreatedAtDesc(customerId, pageable);

        return reports.map(r -> {
            ReportFile file = reportFileRepository.findByReportRequestId(r.getId())
                    .stream().findFirst().orElse(null);
            return mapToResponse(r, file);
        });
    }

    /**
     * Get admin reports with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAdminReports(Pageable pageable) {
        Page<ReportRequest> reports = reportRequestRepository
                .findByUserRoleOrderByCreatedAtDesc(UserRole.ADMIN, pageable);

        return reports.map(r -> {
            ReportFile file = reportFileRepository.findByReportRequestId(r.getId())
                    .stream().findFirst().orElse(null);
            return mapToResponse(r, file);
        });
    }

    /**
     * Get report details by ID (Customer)
     */
    @Transactional(readOnly = true)
    public ReportResponse getCustomerReportById(String reportId, String customerId) {
        ReportRequest report = reportRequestRepository.findByIdAndRequestedBy(Long.valueOf(reportId), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        ReportFile file = reportFileRepository.findByReportRequestId(Long.valueOf(reportId))
                .stream().findFirst().orElse(null);

        return mapToResponse(report, file);
    }

    /**
     * Get report details by ID (Admin)
     */
    @Transactional(readOnly = true)
    public ReportResponse getAdminReportById(String reportId) {
        ReportRequest report = reportRequestRepository.findById(Long.valueOf(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        ReportFile file = reportFileRepository.findByReportRequestId(Long.valueOf(reportId))
                .stream().findFirst().orElse(null);

        return mapToResponse(report, file);
    }

    /**
     * Download report
     */
    @Transactional
    public byte[] downloadReport(String reportId, String userId, UserRole userRole) {
        ReportRequest report = reportRequestRepository.findById(Long.valueOf(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        // Verify access - customers can only download their own reports
        if (userRole == UserRole.CUSTOMER && !report.getRequestedBy().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to download this report");
        }

        // Check if report is ready
        if (report.getStatus() != ReportStatus.COMPLETED) {
            throw new IllegalStateException("Report is not ready for download. Current status: " + report.getStatus());
        }

        // Check if report has expired
        if (report.getExpiresAt() != null && report.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Report has expired and is no longer available");
        }

        try {
            byte[] fileData = Files.readAllBytes(Paths.get(report.getFilePath()));

            // Update download count
            report.setDownloadCount((report.getDownloadCount() != null ? report.getDownloadCount() : 0) + 1);
            reportRequestRepository.save(report);

            log.info("Report {} downloaded by user {}. Download count: {}",
                    reportId, userId, report.getDownloadCount());

            return fileData;

        } catch (IOException e) {
            log.error("Error reading report file: {} at path: {}", reportId, report.getFilePath(), e);
            throw new RuntimeException("Error reading report file: " + e.getMessage());
        }
    }

    /**
     * Delete report (Customer)
     */
    @Transactional
    public void deleteCustomerReport(String reportId, String customerId) {
        ReportRequest report = reportRequestRepository.findByIdAndRequestedBy(Long.valueOf(reportId), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        deleteReportFiles(reportId);
        reportRequestRepository.delete(report);

        log.info("Report {} deleted by customer {}", reportId, customerId);
    }

    /**
     * Delete report (Admin)
     */
    @Transactional
    public void deleteAdminReport(String reportId) {
        ReportRequest report = reportRequestRepository.findById(Long.valueOf(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        deleteReportFiles(reportId);
        reportRequestRepository.delete(report);

        log.info("Report {} deleted by admin", reportId);
    }

    /**
     * Get report status
     */
    @Transactional(readOnly = true)
    public ReportStatusResponse getReportStatus(String reportId) {
        ReportRequest report = reportRequestRepository.findById(Long.valueOf(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        return ReportStatusResponse.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .message(getStatusMessage(report.getStatus()))
                .errorMessage(report.getErrorMessage())
                .build();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generate unique filename for report
     */
    private String generateFileName(ReportRequest request) {
        return String.format("%s_%d_%d.%s",
                request.getReportType().toString().toLowerCase(),
                request.getId(),
                System.currentTimeMillis(),
                request.getFormat().toString().toLowerCase());
    }

    /**
     * Get MIME type for report format
     */
    private String getMimeType(ReportFormat format) {
        return switch (format) {
            case PDF -> "application/pdf";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV -> "text/csv";
        };
    }

    /**
     * ✅ FIX 2: Properly serialize filters using ObjectMapper
     */
    private String serializeFilters(Object filters) {
        if (filters == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(filters);
        } catch (Exception e) {
            log.warn("Error serializing report filters", e);
            return null;
        }
    }

    /**
     * ✅ FIX 3: Generate correct download URL based on user role
     */
    private String generateDownloadUrl(ReportRequest reportRequest) {
        String basePath = reportRequest.getUserRole() == UserRole.CUSTOMER
                ? "/api/v1/customer/reports/download/"
                : "/api/v1/admin/reports/download/";

        return basePath + reportRequest.getId();
    }

    /**
     * Delete report files from storage
     */
    private void deleteReportFiles(String reportRequestId) {
        List<ReportFile> files = reportFileRepository.findByReportRequestId(Long.valueOf(reportRequestId));

        for (ReportFile file : files) {
            try {
                Files.deleteIfExists(Paths.get(file.getStoragePath()));
                log.debug("Report file deleted: {}", file.getStoragePath());
            } catch (IOException e) {
                log.warn("Error deleting report file: {}", file.getStoragePath(), e);
            }
            reportFileRepository.delete(file);
        }
    }

    /**
     * Get user-friendly status message
     */
    private String getStatusMessage(ReportStatus status) {
        return switch (status) {
            case PENDING -> "Your report is queued for generation";
            case PROCESSING -> "Your report is being generated. Please wait...";
            case COMPLETED -> "Your report is ready for download";
            case FAILED -> "Report generation failed. Please try again.";
            case EXPIRED -> "Report has expired and is no longer available";
        };
    }

    /**
     * Map ReportRequest entity to ReportResponse DTO
     */
    private ReportResponse mapToResponse(ReportRequest request, ReportFile file) {
        return ReportResponse.builder()
                .id(request.getId())
                .reportType(request.getReportType())
                .format(request.getFormat())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .downloadUrl(request.getDownloadUrl())
                .generatedAt(request.getGeneratedAt())
                .expiresAt(request.getExpiresAt())
                .downloadCount(request.getDownloadCount())
                .fileSizeInBytes(file != null ? file.getFileSize() : null)
                .mimeType(file != null ? file.getMimeType() : null)
                .createdAt(request.getCreatedAt())
                .errorMessage(request.getErrorMessage())
                .build();
    }
}