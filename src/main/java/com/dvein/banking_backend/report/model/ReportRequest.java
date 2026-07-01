// report/model/ReportRequest.java
package com.dvein.banking_backend.report.model;

import com.dvein.banking_backend.report.enums.ReportFormat;
import com.dvein.banking_backend.report.enums.ReportStatus;
import com.dvein.banking_backend.report.enums.ReportType;
import com.dvein.banking_backend.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_requests", indexes = {
        @Index(name = "idx_report_requested_by", columnList = "requested_by"),
        @Index(name = "idx_report_type", columnList = "report_type"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(nullable = false)
    private String requestedBy; // User ID (Customer or Admin ID)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String filters; // JSON string of filters

    @Column
    private String filePath;

    @Column
    private String downloadUrl;

    @Column
    private LocalDateTime generatedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private Integer downloadCount;

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // In case of failure

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String createdBy;
}