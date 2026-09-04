package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.ReportDto.MonthlyReportResponse
import com.syfe.personalfinance.entity.User
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.impl.ReportServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ReportServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var userService: UserService

    private lateinit var reportService: ReportService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        reportService = ReportServiceImpl(transactionRepository, userService)
        testUser = User(id = 100L, username = "user@syfe.com")
    }

    @Test
    fun getMonthlyReport_Success() {
        val startDate = LocalDate.of(2026, 5, 1)
        val endDate = LocalDate.of(2026, 5, 31)

        val mockIncomeSummary = listOf(
            arrayOf<Any>("Salary", BigDecimal("5000.00")),
            arrayOf<Any>("Freelance", BigDecimal("1200.00"))
        )

        val mockExpenseSummary = listOf(
            arrayOf<Any>("Food", BigDecimal("450.00")),
            arrayOf<Any>("Rent", BigDecimal("1200.00")),
            arrayOf<Any>("Utilities", BigDecimal("150.00"))
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(transactionRepository.sumByCategoryAndDateRange(100L, CategoryType.INCOME, startDate, endDate))
            .thenReturn(mockIncomeSummary)
        whenever(transactionRepository.sumByCategoryAndDateRange(100L, CategoryType.EXPENSE, startDate, endDate))
            .thenReturn(mockExpenseSummary)

        val report: MonthlyReportResponse = reportService.getMonthlyReport(2026, 5)

        assertNotNull(report)
        assertEquals(BigDecimal("4400.00"), report.netSavings)
        assertTrue(report.totalIncome.containsKey("Salary"))
        assertTrue(report.totalExpenses.containsKey("Rent"))
    }
}
