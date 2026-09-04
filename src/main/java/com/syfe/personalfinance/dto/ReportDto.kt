package com.syfe.personalfinance.dto

import java.math.BigDecimal

class ReportDto {

    data class MonthlyReportResponse(
        val month: Int = 0,
        val year: Int = 0,
        val totalIncome: Map<String, BigDecimal> = emptyMap(),
        val totalExpenses: Map<String, BigDecimal> = emptyMap(),
        val netSavings: BigDecimal = BigDecimal.ZERO
    )

    data class YearlyReportResponse(
        val year: Int = 0,
        val totalIncome: Map<String, BigDecimal> = emptyMap(),
        val totalExpenses: Map<String, BigDecimal> = emptyMap(),
        val netSavings: BigDecimal = BigDecimal.ZERO
    )
}
