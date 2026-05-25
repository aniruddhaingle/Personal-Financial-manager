package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.ReportDto.MonthlyReportResponse;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    private ReportService reportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(transactionRepository, userService);
        testUser = User.builder().id(100L).username("user@syfe.com").build();
    }

    @Test
    void getMonthlyReport_Success() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);

        List<Object[]> mockIncomeSummary = Arrays.asList(
                new Object[]{"Salary", new BigDecimal("5000.00")},
                new Object[]{"Freelance", new BigDecimal("1200.00")}
        );

        List<Object[]> mockExpenseSummary = Arrays.asList(
                new Object[]{"Food", new BigDecimal("450.00")},
                new Object[]{"Rent", new BigDecimal("1200.00")},
                new Object[]{"Utilities", new BigDecimal("150.00")}
        );

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(transactionRepository.sumByCategoryAndDateRange(100L, CategoryType.INCOME, startDate, endDate))
                .thenReturn(mockIncomeSummary);
        when(transactionRepository.sumByCategoryAndDateRange(100L, CategoryType.EXPENSE, startDate, endDate))
                .thenReturn(mockExpenseSummary);

        MonthlyReportResponse report = reportService.getMonthlyReport(2026, 5);

        assertNotNull(report);
        assertEquals(new BigDecimal("4400.00"), report.getNetSavings());
        assertTrue(report.getTotalIncome().containsKey("Salary"));
        assertTrue(report.getTotalExpenses().containsKey("Rent"));
    }
}
