package com.syfe.personalfinance.controller

import com.syfe.personalfinance.dto.ReportDto.MonthlyReportResponse
import com.syfe.personalfinance.dto.ReportDto.YearlyReportResponse
import com.syfe.personalfinance.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller providing monthly and yearly financial analytics and breakdown reports.
 */
@RestController
@RequestMapping("/api/reports")
class ReportController(
    private val reportService: ReportService
) {

    /**
     * Aggregates total income, expense breakdown by category, and net savings for a specific year and month.
     */
    @GetMapping("/monthly/{year}/{month}")
    fun getMonthlyReport(
        @PathVariable year: Int,
        @PathVariable month: Int
    ): ResponseEntity<MonthlyReportResponse> {
        val responseData = reportService.getMonthlyReport(year, month)
        return ResponseEntity.ok(responseData)
    }

    /**
     * Aggregates annual financial metrics and category breakdowns for a given calendar year.
     */
    @GetMapping("/yearly/{year}")
    fun getYearlyReport(@PathVariable year: Int): ResponseEntity<YearlyReportResponse> {
        val responseData = reportService.getYearlyReport(year)
        return ResponseEntity.ok(responseData)
    }
}
