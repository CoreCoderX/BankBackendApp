package com.dvein.banking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDTO {

    private long totalCustomers;
    private long totalAccounts;
    private long totalTransactions;
    private long totalLoans;
    private long totalFundTransfers;

}