package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.SavingsGoalDto
import com.syfe.personalfinance.dto.SavingsGoalDto.GoalProgressResponse
import com.syfe.personalfinance.entity.SavingsGoal
import com.syfe.personalfinance.entity.User
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.mapper.SavingsGoalMapper
import com.syfe.personalfinance.repository.SavingsGoalRepository
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.impl.SavingsGoalServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class SavingsGoalServiceTest {

    @Mock
    private lateinit var savingsGoalRepository: SavingsGoalRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var savingsGoalMapper: SavingsGoalMapper

    @Mock
    private lateinit var userService: UserService

    private lateinit var savingsGoalService: SavingsGoalService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        savingsGoalService = SavingsGoalServiceImpl(savingsGoalRepository, transactionRepository, savingsGoalMapper, userService)
        testUser = User(id = 100L, username = "user@syfe.com")
    }

    @Test
    fun createGoal_Success() {
        val request = SavingsGoalDto.CreateGoalRequest(
            goalName = "Car fund",
            targetAmount = BigDecimal("10000.00"),
            startDate = LocalDate.now(),
            targetDate = LocalDate.now().plusYears(1)
        )

        val goalEntity = SavingsGoal(
            goalName = "Car fund",
            targetAmount = BigDecimal("10000.00"),
            startDate = LocalDate.now(),
            targetDate = LocalDate.now().plusYears(1),
            user = testUser
        )

        val expectedResponse = GoalProgressResponse(
            id = 1L,
            goalName = "Car fund",
            targetAmount = BigDecimal("10000.00")
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(savingsGoalMapper.toEntity(request)).thenReturn(goalEntity)
        whenever(savingsGoalRepository.save(any<SavingsGoal>())).thenReturn(goalEntity)
        whenever(savingsGoalMapper.toProgressResponse(any<SavingsGoal>(), any(), any(), any())).thenReturn(expectedResponse)

        val response = savingsGoalService.createGoal(request)

        assertNotNull(response)
        assertEquals("Car fund", response.goalName)
        verify(savingsGoalRepository, times(1)).save(any<SavingsGoal>())
    }

    @Test
    fun createGoal_TargetBeforeStart_ThrowsException() {
        val request = SavingsGoalDto.CreateGoalRequest(
            goalName = "Car fund",
            targetAmount = BigDecimal("10000.00"),
            startDate = LocalDate.now(),
            targetDate = LocalDate.now().minusDays(5)
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)

        assertThrows(BadRequestException::class.java) { savingsGoalService.createGoal(request) }
        verify(savingsGoalRepository, never()).save(any<SavingsGoal>())
    }

    @Test
    fun getGoalProgress_SuccessCalculations() {
        val startDate = LocalDate.now().minusMonths(3)
        val goal = SavingsGoal(
            id = 1L,
            goalName = "Car fund",
            targetAmount = BigDecimal("10000.00"),
            startDate = startDate,
            targetDate = LocalDate.now().plusYears(1),
            user = testUser
        )

        val expectedProgress = GoalProgressResponse(
            id = 1L,
            goalName = "Car fund",
            targetAmount = BigDecimal("10000.00"),
            currentProgress = BigDecimal("3000.00"),
            progressPercentage = 30.0,
            remainingAmount = BigDecimal("7000.00")
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(savingsGoalRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(goal))

        whenever(transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(100L, CategoryType.INCOME, startDate))
            .thenReturn(BigDecimal("5000.00"))
        whenever(transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(100L, CategoryType.EXPENSE, startDate))
            .thenReturn(BigDecimal("2000.00"))

        whenever(savingsGoalMapper.toProgressResponse(eq(goal), any(), any(), any()))
            .thenReturn(expectedProgress)

        val progress = savingsGoalService.getGoalProgress(1L)

        assertNotNull(progress)
        assertEquals(BigDecimal("3000.00"), progress.currentProgress)
        assertEquals(30.0, progress.progressPercentage)
        assertEquals(BigDecimal("7000.00"), progress.remainingAmount)
    }
}
