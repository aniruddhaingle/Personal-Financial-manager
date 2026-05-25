package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.ApiResponse;
import com.syfe.personalfinance.dto.ReportDto.ReportResponse;
import com.syfe.personalfinance.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    // Constructor injection only
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<ReportResponse>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {

        ReportResponse responseData = reportService.getMonthlyReport(year, month);
        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Monthly report generated successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<ReportResponse>> getYearlyReport(@RequestParam int year) {
        ReportResponse responseData = reportService.getYearlyReport(year);
        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Yearly report generated successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
