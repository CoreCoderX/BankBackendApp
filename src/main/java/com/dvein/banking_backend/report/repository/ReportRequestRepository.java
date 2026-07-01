// report/repository/ReportRequestRepository.java
package com.dvein.banking_backend.report.repository;

import com.dvein.banking_backend.report.model.ReportRequest;
import com.dvein.banking_backend.report.enums.ReportStatus;
import com.dvein.banking_backend.report.enums.ReportType;
import com.dvein.banking_backend.common.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRequestRepository extends JpaRepository<ReportRequest, Long> {

    // Customer queries
    Page<ReportRequest> findByRequestedByOrderByCreatedAtDesc(String requestedBy, Pageable pageable);

    Page<ReportRequest> findByRequestedByAndStatusOrderByCreatedAtDesc(
            String requestedBy,
            ReportStatus status,
            Pageable pageable
    );

    Optional<ReportRequest> findByIdAndRequestedBy(Long id, String requestedBy);

    List<ReportRequest> findByRequestedByAndStatusAndExpiresAtBefore(
            String requestedBy,
            ReportStatus status,
            LocalDateTime expiresAt
    );

    // Admin queries
    Page<ReportRequest> findByUserRoleOrderByCreatedAtDesc(UserRole userRole, Pageable pageable);

    Page<ReportRequest> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Page<ReportRequest> findByReportTypeAndStatusOrderByCreatedAtDesc(
            ReportType reportType,
            ReportStatus status,
            Pageable pageable
    );

    // Cleanup queries
    List<ReportRequest> findByStatusAndExpiresAtBefore(ReportStatus status, LocalDateTime expiresAt);

    @Query("SELECT r FROM ReportRequest r WHERE r.status = :status AND r.createdAt < :dateTime")
    List<ReportRequest> findOldExpiredReports(
            @Param("status") ReportStatus status,
            @Param("dateTime") LocalDateTime dateTime
    );

    // Count queries
    @Query("SELECT COUNT(r) FROM ReportRequest r WHERE r.requestedBy = :userId AND r.status = :status")
    long countByRequestedByAndStatus(
            @Param("userId") String userId,
            @Param("status") ReportStatus status
    );

    @Query("SELECT COUNT(r) FROM ReportRequest r WHERE r.reportType = :reportType AND r.status = :status")
    long countByReportTypeAndStatus(
            @Param("reportType") ReportType reportType,
            @Param("status") ReportStatus status
    );
}