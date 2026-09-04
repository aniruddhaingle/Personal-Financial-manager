package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.SavingsGoalDto

/**
 * Service managing user savings goals and tracking progress against net income.
 */
interface SavingsGoalService {
    /**
     * Establishes a new savings goal with target parameters.
     *
     * @param request target amount, target date, and optional start date
     * @return savings goal progress details
     */
    fun createGoal(request: SavingsGoalDto.CreateGoalRequest): SavingsGoalDto.GoalProgressResponse

    /**
     * Retrieves all savings goals for the user with dynamic calculation of progress.
     *
     * @return list of goal progress records
     */
    fun getAllGoalsProgress(): List<SavingsGoalDto.GoalProgressResponse>

    /**
     * Retrieves the progress details for a specific savings goal.
     *
     * @param id savings goal ID
     * @return goal progress details
     * @throws com.syfe.personalfinance.exception.ResourceNotFoundException if goal not found or unowned
     */
    fun getGoalProgress(id: Long): SavingsGoalDto.GoalProgressResponse

    /**
     * Updates target amount, target date, or start date of an existing goal.
     *
     * @param id savings goal ID
     * @param request modified goal properties
     * @return updated goal progress details
     * @throws com.syfe.personalfinance.exception.ResourceNotFoundException if goal not found or unowned
     */
    fun updateGoal(id: Long, request: SavingsGoalDto.UpdateGoalRequest): SavingsGoalDto.GoalProgressResponse

    /**
     * Deletes a savings goal by ID for the authenticated user.
     *
     * @param id savings goal ID
     * @throws com.syfe.personalfinance.exception.ResourceNotFoundException if goal not found or unowned
     */
    fun deleteGoal(id: Long)
}

