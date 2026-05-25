package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.ReportDto;

public interface ReportService {
    ReportDto.MonthlyReportResponse getMonthlyReport(int year, int month);
    ReportDto.YearlyReportResponse getYearlyReport(int year);
}
