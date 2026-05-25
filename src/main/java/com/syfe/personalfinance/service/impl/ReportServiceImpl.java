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

    // Constructor injection only
    public ReportServiceImpl(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.ReportResponse getMonthlyReport(int year, int month) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Generating monthly report for {}-{} and user ID: {}", year, month, currentUser.getId());

        // Resolve dates natively using LocalDate details
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());

        return generateReport(currentUser, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.ReportResponse getYearlyReport(int year) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Generating yearly report for year {} and user ID: {}", year, currentUser.getId());

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return generateReport(currentUser, startDate, endDate);
    }

    // Shared Helper: Calculates categories groupings and net savings within a timeframe
    private ReportDto.ReportResponse generateReport(User user, LocalDate startDate, LocalDate endDate) {
        // Fetch aggregated values from database grouped by category names
        List<Object[]> incomeList = transactionRepository.sumByCategoryAndDateRange(user.getId(), CategoryType.INCOME, startDate, endDate);
        List<Object[]> expenseList = transactionRepository.sumByCategoryAndDateRange(user.getId(), CategoryType.EXPENSE, startDate, endDate);

        Map<String, BigDecimal> incomeByCategory = convertToMap(incomeList);
        Map<String, BigDecimal> expenseByCategory = convertToMap(expenseList);

        // Sum up total income and expense amounts
        BigDecimal totalIncome = incomeByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenseByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Formula: net savings = total income - total expenses
        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        log.debug("Report aggregated: totalIncome={}, totalExpense={}, netSavings={}", totalIncome, totalExpense, netSavings);

        return ReportDto.ReportResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .build();
    }

    // Convert SQL projection results list into category mappings
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
