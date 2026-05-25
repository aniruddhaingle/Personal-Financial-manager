package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.ReportDto;

public interface ReportService {
    ReportDto.ReportResponse getMonthlyReport(int year, int month);
    ReportDto.ReportResponse getYearlyReport(int year);
}
