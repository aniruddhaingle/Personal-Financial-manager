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
    public static class ReportResponse {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netSavings;
        private Map<String, BigDecimal> incomeByCategory;
        private Map<String, BigDecimal> expenseByCategory;
    }
}
