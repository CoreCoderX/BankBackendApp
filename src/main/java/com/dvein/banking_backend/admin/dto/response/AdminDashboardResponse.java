package com.dvein.banking_backend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin dashboard statistics response")
public class AdminDashboardResponse {

    @Schema(description = "Total customers", example = "1000")
    private long totalCustomers;

    @Schema(description = "Active customers", example = "950")
    private long activeCustomers;

    @Schema(description = "Blocked customers", example = "50")
    private long blockedCustomers;

    @Schema(description = "Suspended customers", example = "20")
    private long suspendedCustomers;

    @Schema(description = "Total admins", example = "5")
    private long totalAdmins;

    @Schema(description = "Active admins", example = "5")
    private long activeAdmins;

    @Schema(description = "Total accounts", example = "1500")
    private long totalAccounts;

    @Schema(description = "Total balance across all accounts")
    private BigDecimal totalBalance;

    @Schema(description = "Pending KYC approvals", example = "25")
    private long pendingKycApprovals;

    @Schema(description = "Pending credit card applications", example = "10")
    private long pendingCreditCardApplications;

    @Schema(description = "Total debit cards", example = "800")
    private long totalDebitCards;

    @Schema(description = "Total credit cards", example = "200")
    private long totalCreditCards;

    @Schema(description = "Failed login attempts today", example = "15")
    private long failedLoginAttemptsToday;

    @Schema(description = "New registrations today", example = "5")
    private long newRegistrationsToday;

    @Schema(description = "Active sessions", example = "350")
    private long activeSessions;
}