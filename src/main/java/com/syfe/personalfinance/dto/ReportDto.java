package com.syfe.personalfinance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

public class ReportDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyReportResponse {
        private int month;
        private int year;
        private Map<String, BigDecimal> totalIncome;
        private Map<String, BigDecimal> totalExpenses;
        private BigDecimal netSavings;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YearlyReportResponse {
        private int year;
        private Map<String, BigDecimal> totalIncome;
        private Map<String, BigDecimal> totalExpenses;
        private BigDecimal netSavings;
    }
}
