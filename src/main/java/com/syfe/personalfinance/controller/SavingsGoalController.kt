package com.syfe.personalfinance.controller

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.dto.SavingsGoalDto
import com.syfe.personalfinance.service.SavingsGoalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller managing personal savings goals, tracking ongoing progress, and metrics.
 */
@RestController
@RequestMapping("/api/goals")
class SavingsGoalController(
    private val savingsGoalService: SavingsGoalService
) {

    /**
     * Establishes a new savings target with target amount and future completion date.
     */
    @PostMapping
    fun createGoal(
        @Valid @RequestBody request: SavingsGoalDto.CreateGoalRequest
    ): ResponseEntity<SavingsGoalDto.GoalProgressResponse> {
        val responseData = savingsGoalService.createGoal(request)
        return ResponseEntity(responseData, HttpStatus.CREATED)
    }

    /**
     * Lists all savings goals for the user with calculated progress, percentages, and remaining amounts.
     */
    @GetMapping
    fun getAllGoalsProgress(): ResponseEntity<SavingsGoalDto.GoalListResponse> {
        val responseData = savingsGoalService.getAllGoalsProgress()
        val response = SavingsGoalDto.GoalListResponse(
            goals = responseData
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Fetches details and progress metrics for a single specific goal.
     */
    @GetMapping("/{id}")
    fun getGoalProgress(@PathVariable id: Long): ResponseEntity<SavingsGoalDto.GoalProgressResponse> {
        val responseData = savingsGoalService.getGoalProgress(id)
        return ResponseEntity.ok(responseData)
    }

    /**
     * Updates target parameters of an existing savings goal.
     */
    @PutMapping("/{id}")
    fun updateGoal(
        @PathVariable id: Long,
        @Valid @RequestBody request: SavingsGoalDto.UpdateGoalRequest
    ): ResponseEntity<SavingsGoalDto.GoalProgressResponse> {
        val responseData = savingsGoalService.updateGoal(id, request)
        return ResponseEntity.ok(responseData)
    }

    /**
     * Deletes a savings goal by its identifier.
     */
    @DeleteMapping("/{id}")
    fun deleteGoal(@PathVariable id: Long): ResponseEntity<AuthDto.SimpleMessageResponse> {
        savingsGoalService.deleteGoal(id)
        val response = AuthDto.SimpleMessageResponse(
            message = "Goal deleted successfully"
        )
        return ResponseEntity.ok(response)
    }
}
