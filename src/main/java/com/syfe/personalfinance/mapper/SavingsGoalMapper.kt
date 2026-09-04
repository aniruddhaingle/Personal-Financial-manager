package com.syfe.personalfinance.mapper

import com.syfe.personalfinance.dto.SavingsGoalDto
import com.syfe.personalfinance.entity.SavingsGoal
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class SavingsGoalMapper {

    fun toProgressResponse(
        goal: SavingsGoal?,
        currentProgress: BigDecimal?,
        progressPercentage: Double?,
        remainingAmount: BigDecimal?
    ): SavingsGoalDto.GoalProgressResponse? {
        if (goal == null) {
            return null
        }
        return SavingsGoalDto.GoalProgressResponse(
            id = goal.id,
            goalName = goal.goalName,
            targetAmount = goal.targetAmount,
            startDate = goal.startDate,
            targetDate = goal.targetDate,
            currentProgress = currentProgress,
            progressPercentage = progressPercentage,
            remainingAmount = remainingAmount
        )
    }

    fun toEntity(request: SavingsGoalDto.CreateGoalRequest?): SavingsGoal? {
        if (request == null) {
            return null
        }
        return SavingsGoal(
            goalName = request.goalName,
            targetAmount = request.targetAmount ?: BigDecimal.ZERO,
            startDate = request.startDate ?: LocalDate.now(),
            targetDate = request.targetDate ?: LocalDate.now()
        )
    }
}
