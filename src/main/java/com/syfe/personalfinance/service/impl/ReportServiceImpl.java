package com.syfe.personalfinance.service.impl;

import com.syfe.personalfinance.dto.ReportDto;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.ReportService;
import com.syfe.personalfinance.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public ReportServiceImpl(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.MonthlyReportResponse getMonthlyReport(int year, int month) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Generating monthly report for {}-{} and user ID: {}", year, month, currentUser.getId());

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());

        List<Object[]> incomeList = transactionRepository.sumByCategoryAndDateRange(currentUser.getId(), CategoryType.INCOME, startDate, endDate);
        List<Object[]> expenseList = transactionRepository.sumByCategoryAndDateRange(currentUser.getId(), CategoryType.EXPENSE, startDate, endDate);

        Map<String, BigDecimal> totalIncome = convertToMap(incomeList);
        Map<String, BigDecimal> totalExpenses = convertToMap(expenseList);

        BigDecimal sumIncome = totalIncome.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumExpense = totalExpenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = sumIncome.subtract(sumExpense);

        return ReportDto.MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.YearlyReportResponse getYearlyReport(int year) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Generating yearly report for year {} and user ID: {}", year, currentUser.getId());

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Object[]> incomeList = transactionRepository.sumByCategoryAndDateRange(currentUser.getId(), CategoryType.INCOME, startDate, endDate);
        List<Object[]> expenseList = transactionRepository.sumByCategoryAndDateRange(currentUser.getId(), CategoryType.EXPENSE, startDate, endDate);

        Map<String, BigDecimal> totalIncome = convertToMap(incomeList);
        Map<String, BigDecimal> totalExpenses = convertToMap(expenseList);

        BigDecimal sumIncome = totalIncome.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumExpense = totalExpenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = sumIncome.subtract(sumExpense);

        return ReportDto.YearlyReportResponse.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }

    private Map<String, BigDecimal> convertToMap(List<Object[]> queryResults) {
        Map<String, BigDecimal> summaryMap = new HashMap<>();
        if (queryResults != null) {
            for (Object[] result : queryResults) {
                String categoryName = (String) result[0];
                BigDecimal sumAmount = (BigDecimal) result[1];
                summaryMap.put(categoryName, sumAmount);
            }
        }
        return summaryMap;
    }
}
