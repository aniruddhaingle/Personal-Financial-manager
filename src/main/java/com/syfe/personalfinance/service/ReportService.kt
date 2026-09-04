package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.ReportDto

/**
 * Service calculating aggregate monthly and yearly personal financial analytics and breakdown.
 */
interface ReportService {
    /**
     * Aggregates total incomes and expenses grouped by category for a specific month/year,
     * computing the net savings (income - expenses).
     *
     * @param year the 4-digit year (e.g., 2026)
     * @param month the month of year (1-12)
     * @return the monthly financial breakdown
     * @throws com.syfe.personalfinance.exception.BadRequestException if month is not in range 1-12
     */
    fun getMonthlyReport(year: Int, month: Int): ReportDto.MonthlyReportResponse

    /**
     * Aggregates financial totals across the entire calendar year for the authenticated user.
     *
     * @param year the 4-digit year (e.g., 2026)
     * @return the yearly financial summary
     */
    fun getYearlyReport(year: Int): ReportDto.YearlyReportResponse
}

