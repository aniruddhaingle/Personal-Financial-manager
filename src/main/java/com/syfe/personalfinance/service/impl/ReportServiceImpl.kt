package com.syfe.personalfinance.service.impl

import com.syfe.personalfinance.dto.ReportDto
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.ReportService
import com.syfe.personalfinance.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ReportServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val userService: UserService
) : ReportService {

    private val log = LoggerFactory.getLogger(ReportServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun getMonthlyReport(year: Int, month: Int): ReportDto.MonthlyReportResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Generating monthly report for {}-{} and user ID: {}", year, month, currentUser.id)

        if (month < 1 || month > 12) {
            throw BadRequestException("Invalid value for MonthOfYear (valid values 1 - 12): $month")
        }

        val startDate = LocalDate.of(year, month, 1)
        val endDate = LocalDate.of(year, month, startDate.lengthOfMonth())

        val incomeList = transactionRepository.sumByCategoryAndDateRange(
            currentUser.id!!, CategoryType.INCOME, startDate, endDate
        )
        val expenseList = transactionRepository.sumByCategoryAndDateRange(
            currentUser.id!!, CategoryType.EXPENSE, startDate, endDate
        )

        val totalIncome = convertToMap(incomeList)
        val totalExpenses = convertToMap(expenseList)

        val sumIncome = totalIncome.values.fold(BigDecimal.ZERO, BigDecimal::add)
        val sumExpense = totalExpenses.values.fold(BigDecimal.ZERO, BigDecimal::add)
        val netSavings = sumIncome.subtract(sumExpense)

        return ReportDto.MonthlyReportResponse(
            month = month,
            year = year,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavings = netSavings
        )
    }

    @Transactional(readOnly = true)
    override fun getYearlyReport(year: Int): ReportDto.YearlyReportResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Generating yearly report for year {} and user ID: {}", year, currentUser.id)

        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)

        val incomeList = transactionRepository.sumByCategoryAndDateRange(
            currentUser.id!!, CategoryType.INCOME, startDate, endDate
        )
        val expenseList = transactionRepository.sumByCategoryAndDateRange(
            currentUser.id!!, CategoryType.EXPENSE, startDate, endDate
        )

        val totalIncome = convertToMap(incomeList)
        val totalExpenses = convertToMap(expenseList)

        val sumIncome = totalIncome.values.fold(BigDecimal.ZERO, BigDecimal::add)
        val sumExpense = totalExpenses.values.fold(BigDecimal.ZERO, BigDecimal::add)
        val netSavings = sumIncome.subtract(sumExpense)

        return ReportDto.YearlyReportResponse(
            year = year,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavings = netSavings
        )
    }

    private fun convertToMap(queryResults: List<Array<Any>>?): Map<String, BigDecimal> {
        val summaryMap = mutableMapOf<String, BigDecimal>()
        if (queryResults != null) {
            for (result in queryResults) {
                val categoryName = result[0] as String
                val sumAmount = result[1] as BigDecimal
                summaryMap[categoryName] = sumAmount
            }
        }
        return summaryMap
    }
}
