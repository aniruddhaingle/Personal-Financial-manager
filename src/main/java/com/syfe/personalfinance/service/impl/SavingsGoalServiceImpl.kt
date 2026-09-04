package com.syfe.personalfinance.service.impl

import com.syfe.personalfinance.dto.SavingsGoalDto
import com.syfe.personalfinance.entity.SavingsGoal
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.exception.ResourceNotFoundException
import com.syfe.personalfinance.mapper.SavingsGoalMapper
import com.syfe.personalfinance.repository.SavingsGoalRepository
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.SavingsGoalService
import com.syfe.personalfinance.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class SavingsGoalServiceImpl(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsGoalMapper: SavingsGoalMapper,
    private val userService: UserService
) : SavingsGoalService {

    private val log = LoggerFactory.getLogger(SavingsGoalServiceImpl::class.java)

    @Transactional
    override fun createGoal(request: SavingsGoalDto.CreateGoalRequest): SavingsGoalDto.GoalProgressResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Creating new savings goal '{}' for user ID: {}", request.goalName, currentUser.id)

        val startDate = request.startDate ?: LocalDate.now()
        val targetDate = request.targetDate ?: throw BadRequestException("Target date is required")

        if (targetDate.isBefore(startDate)) {
            log.warn("Goal creation failed: Target date {} is before start date {}", targetDate, startDate)
            throw BadRequestException("Target date cannot be before the start date")
        }

        val savingsGoal = savingsGoalMapper.toEntity(request) ?: throw IllegalStateException("Failed to map savings goal entity")
        savingsGoal.startDate = startDate
        savingsGoal.targetDate = targetDate
        savingsGoal.user = currentUser

        val savedGoal = savingsGoalRepository.save(savingsGoal)
        log.info("Savings goal created successfully with ID: {}", savedGoal.id)

        return calculateGoalProgress(savedGoal)
    }

    @Transactional(readOnly = true)
    override fun getAllGoalsProgress(): List<SavingsGoalDto.GoalProgressResponse> {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.debug("Calculating dynamic savings progress for all goals of user ID: {}", currentUser.id)

        val goals = savingsGoalRepository.findByUserId(currentUser.id!!)

        return goals.map { calculateGoalProgress(it) }
    }

    @Transactional(readOnly = true)
    override fun getGoalProgress(id: Long): SavingsGoalDto.GoalProgressResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.debug("Calculating dynamic savings progress for goal ID: {} and user ID: {}", id, currentUser.id)

        val goal = savingsGoalRepository.findByIdAndUserId(id, currentUser.id!!)
            .orElseThrow {
                log.warn("Goal progress lookup failed: ID {} not found for user ID: {}", id, currentUser.id)
                ResourceNotFoundException("Savings goal not found or unauthorized")
            }

        return calculateGoalProgress(goal)
    }

    @Transactional
    override fun updateGoal(id: Long, request: SavingsGoalDto.UpdateGoalRequest): SavingsGoalDto.GoalProgressResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Updating savings goal ID: {} for user ID: {}", id, currentUser.id)

        val goal = savingsGoalRepository.findByIdAndUserId(id, currentUser.id!!)
            .orElseThrow {
                log.warn("Goal update failed: ID {} not found for user ID: {}", id, currentUser.id)
                ResourceNotFoundException("Savings goal not found or unauthorized")
            }

        if (request.goalName != null) {
            goal.goalName = request.goalName
        }

        if (request.targetAmount != null) {
            goal.targetAmount = request.targetAmount
        }

        if (request.startDate != null) {
            goal.startDate = request.startDate
        }

        if (request.targetDate != null) {
            goal.targetDate = request.targetDate
        }

        if (goal.targetDate.isBefore(goal.startDate)) {
            log.warn("Goal update failed: Target date {} is before start date {}", goal.targetDate, goal.startDate)
            throw BadRequestException("Target date cannot be before the start date")
        }

        val updatedGoal = savingsGoalRepository.save(goal)
        log.info("Savings goal ID: {} updated successfully", updatedGoal.id)

        return calculateGoalProgress(updatedGoal)
    }

    @Transactional
    override fun deleteGoal(id: Long) {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Deleting savings goal ID: {} for user ID: {}", id, currentUser.id)

        val goal = savingsGoalRepository.findByIdAndUserId(id, currentUser.id!!)
            .orElseThrow {
                log.warn("Goal deletion failed: ID {} not found for user ID: {}", id, currentUser.id)
                ResourceNotFoundException("Savings goal not found or unauthorized")
            }

        savingsGoalRepository.delete(goal)
        log.info("Savings goal ID: {} deleted successfully", id)
    }

    private fun calculateGoalProgress(goal: SavingsGoal): SavingsGoalDto.GoalProgressResponse {
        val userId = goal.user?.id ?: throw IllegalStateException("Goal user is null")
        val totalIncome = transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(
            userId, CategoryType.INCOME, goal.startDate
        ) ?: BigDecimal.ZERO

        val totalExpense = transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(
            userId, CategoryType.EXPENSE, goal.startDate
        ) ?: BigDecimal.ZERO

        val currentProgress = totalIncome.subtract(totalExpense)

        var progressPercentage = BigDecimal.ZERO
        if (goal.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                .multiply(BigDecimal(100))
                .divide(goal.targetAmount, 2, RoundingMode.HALF_UP)
        }

        val remainingAmount = goal.targetAmount.subtract(currentProgress)

        return savingsGoalMapper.toProgressResponse(
            goal, currentProgress, progressPercentage.toDouble(), remainingAmount
        ) ?: throw IllegalStateException("Failed to map goal progress response")
    }
}
