package com.dvein.banking_backend.controller;

import com.dvein.banking_backend.dto.AdminDashboardDTO;
import com.dvein.banking_backend.dto.RecentTransactionDTO;
import com.dvein.banking_backend.dto.AlertDTO;
import com.dvein.banking_backend.dto.AnalyticsDTO;
import com.dvein.banking_backend.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public AdminDashboardDTO getSummary() {

        return dashboardService.getDashboardSummary();
    }
    @GetMapping("/recent-transactions")
    public List<RecentTransactionDTO> getRecentTransactions() {

        return dashboardService.getRecentTransactions();
    }

    @GetMapping("/alerts")
    public List<AlertDTO> getAlerts() {

        return dashboardService.getAlerts();
    }

    @GetMapping("/analytics")
    public List<AnalyticsDTO> getAnalytics() {

        return dashboardService.getAnalytics();
    }
}