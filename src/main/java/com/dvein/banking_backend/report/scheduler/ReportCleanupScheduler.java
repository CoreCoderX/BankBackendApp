// report/scheduler/ReportCleanupScheduler.java
package com.dvein.banking_backend.report.scheduler;

import com.dvein.banking_backend.report.enums.ReportStatus;
import com.dvein.banking_backend.report.model.ReportFile;
import com.dvein.banking_backend.report.model.ReportRequest;
import com.dvein.banking_backend.report.repository.ReportFileRepository;
import com.dvein.banking_backend.report.repository.ReportRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportCleanupScheduler {

    private final ReportRequestRepository reportRequestRepository;
    private final ReportFileRepository reportFileRepository;

    @Value("${report.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${report.file.retention.days:30}")
    private int fileRetentionDays;

    /**
     * Run cleanup every day at 2 AM
     * Deletes expired reports and old files
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredReports() {
        if (!cleanupEnabled) {
            return;
        }

        log.info("Starting report cleanup scheduler");

        try {
            // Delete expired reports
            LocalDateTime expiryTime = LocalDateTime.now();
            List<ReportRequest> expiredReports = reportRequestRepository
                    .findByStatusAndExpiresAtBefore(ReportStatus.COMPLETED, expiryTime);

            for (ReportRequest report : expiredReports) {
                deleteReportAndFiles(report);
            }

            log.info("Deleted {} expired reports", expiredReports.size());

            // Delete failed reports older than 7 days
            LocalDateTime failedReportThreshold = LocalDateTime.now().minusDays(7);
            List<ReportRequest> oldFailedReports = reportRequestRepository
                    .findOldExpiredReports(ReportStatus.FAILED, failedReportThreshold);

            for (ReportRequest report : oldFailedReports) {
                reportRequestRepository.delete(report);
            }

            log.info("Deleted {} old failed reports", oldFailedReports.size());

            // Delete old files
            LocalDateTime fileRetentionThreshold = LocalDateTime.now().minusDays(fileRetentionDays);
            List<ReportFile> oldFiles = reportFileRepository.findOldFiles(fileRetentionThreshold);

            for (ReportFile file : oldFiles) {
                try {
                    Files.deleteIfExists(Paths.get(file.getStoragePath()));
                    reportFileRepository.delete(file);
                } catch (IOException e) {
                    log.warn("Error deleting file: {}", file.getStoragePath(), e);
                }
            }

            log.info("Deleted {} old report files", oldFiles.size());

        } catch (Exception e) {
            log.error("Error in report cleanup scheduler", e);
        }
    }

    /**
     * Mark expired reports as EXPIRED (runs every 6 hours)
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    @Transactional
    public void markExpiredReports() {
        if (!cleanupEnabled) {
            return;
        }

        log.info("Marking expired reports");

        try {
            List<ReportRequest> completedReports = reportRequestRepository
                    .findAll();

            for (ReportRequest report : completedReports) {
                if (report.getStatus() == ReportStatus.COMPLETED &&
                        report.getExpiresAt() != null &&
                        report.getExpiresAt().isBefore(LocalDateTime.now())) {

                    report.setStatus(ReportStatus.EXPIRED);
                    reportRequestRepository.save(report);
                }
            }

        } catch (Exception e) {
            log.error("Error marking expired reports", e);
        }
    }

    private void deleteReportAndFiles(ReportRequest report) {
        List<ReportFile> files = reportFileRepository.findByReportRequestId(report.getId());

        for (ReportFile file : files) {
            try {
                Files.deleteIfExists(Paths.get(file.getStoragePath()));
                reportFileRepository.delete(file);
            } catch (IOException e) {
                log.warn("Error deleting file: {}", file.getStoragePath(), e);
            }
        }

        reportRequestRepository.delete(report);
    }
}