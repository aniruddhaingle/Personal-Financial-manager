package com.syfe.personalfinance.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

class SavingsGoalDto {

    data class CreateGoalRequest(
        @field:NotBlank(message = "Goal name is required")
        val goalName: String = "",

        @field:NotNull(message = "Target amount is required")
        @field:Positive(message = "Target amount must be a positive number")
        val targetAmount: BigDecimal? = null,

        val startDate: LocalDate? = null,

        @field:NotNull(message = "Target date is required")
        @field:Future(message = "Target date must be in the future")
        val targetDate: LocalDate? = null
    )

    data class UpdateGoalRequest(
        val goalName: String? = null,

        @field:Positive(message = "Target amount must be a positive number")
        val targetAmount: BigDecimal? = null,

        val startDate: LocalDate? = null,

        @field:Future(message = "Target date must be in the future")
        val targetDate: LocalDate? = null
    )

    data class GoalProgressResponse(
        val id: Long? = null,
        val goalName: String? = null,
        val targetAmount: BigDecimal? = null,
        val startDate: LocalDate? = null,
        val targetDate: LocalDate? = null,
        val currentProgress: BigDecimal? = null,
        val progressPercentage: Double? = null,
        val remainingAmount: BigDecimal? = null
    )

    data class GoalListResponse(
        val goals: List<GoalProgressResponse> = emptyList()
    )
}
