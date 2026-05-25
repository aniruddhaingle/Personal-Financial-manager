package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.ReportDto.MonthlyReportResponse;
import com.syfe.personalfinance.dto.ReportDto.YearlyReportResponse;
import com.syfe.personalfinance.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {

        MonthlyReportResponse responseData = reportService.getMonthlyReport(year, month);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        YearlyReportResponse responseData = reportService.getYearlyReport(year);
        return ResponseEntity.ok(responseData);
    }
}
