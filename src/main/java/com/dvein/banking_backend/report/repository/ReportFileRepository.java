// report/repository/ReportFileRepository.java
package com.dvein.banking_backend.report.repository;

import com.dvein.banking_backend.report.model.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportFileRepository extends JpaRepository<ReportFile, Long> {

    List<ReportFile> findByReportRequestId(Long reportRequestId);

    Optional<ReportFile> findByIdAndReportRequestId(Long fileId, Long reportRequestId);

    @Query("SELECT r FROM ReportFile r WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<ReportFile> findAllActiveFiles();

    @Query("SELECT r FROM ReportFile r WHERE r.createdAt < :dateTime AND r.deletedAt IS NULL")
    List<ReportFile> findOldFiles(@Param("dateTime") LocalDateTime dateTime);
}