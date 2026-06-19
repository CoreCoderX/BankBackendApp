package com.dvein.banking_backend.service;

import com.dvein.banking_backend.dto.*;
import java.util.List;

public interface DashboardService {

    AdminDashboardDTO getDashboardSummary();

    List<RecentTransactionDTO> getRecentTransactions();

    List<AlertDTO> getAlerts();

    List<AnalyticsDTO> getAnalytics();
}