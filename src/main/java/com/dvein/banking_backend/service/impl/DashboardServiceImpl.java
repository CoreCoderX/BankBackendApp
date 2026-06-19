package com.dvein.banking_backend.service.impl;

import com.dvein.banking_backend.dto.*;
import com.dvein.banking_backend.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public AdminDashboardDTO getDashboardSummary() {

        return new AdminDashboardDTO(
                12450,
                18230,
                125000,
                2450,
                35000
        );
    }

    @Override
    public List<RecentTransactionDTO> getRecentTransactions() {

        return List.of(
                new RecentTransactionDTO(
                        1001L,
                        "Novin",
                        5000.0,
                        "CREDIT"
                ),
                new RecentTransactionDTO(
                        1002L,
                        "Rahul",
                        2500.0,
                        "DEBIT"
                )
        );
    }

    @Override
    public List<AlertDTO> getAlerts() {

        return List.of(
                new AlertDTO("LOAN", "120 pending loans"),
                new AlertDTO("ACCOUNT", "15 blocked accounts"),
                new AlertDTO("SECURITY", "3 suspicious transactions")
        );
    }

    @Override
    public List<AnalyticsDTO> getAnalytics() {

        return List.of(
                new AnalyticsDTO("JAN", 1200L),
                new AnalyticsDTO("FEB", 1800L),
                new AnalyticsDTO("MAR", 2400L)
        );
    }
}