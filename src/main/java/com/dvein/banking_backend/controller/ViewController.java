package com.dvein.banking_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/customer/home")
    public String customerHome() {
        return "customer/home";
    }

    @GetMapping("/customer/transactions")
    public String transactions() {
        return "customer/transactions";
    }

    @GetMapping("/customer/transfer")
    public String transfer() {
        return "customer/transfer";
    }

    @GetMapping("/customer/loans")
    public String loans() {
        return "customer/loans";
    }

    @GetMapping("/customer/profile")
    public String profile() {
        return "customer/profile";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/reports")
    public String reports() {
        return "admin/reports";
    }

    @GetMapping("/admin/analytics")
    public String analytics() {
        return "admin/analytics";
    }
}