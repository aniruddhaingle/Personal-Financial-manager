package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.ApiResponse;
import com.syfe.personalfinance.dto.SavingsGoalDto;
import com.syfe.personalfinance.dto.SavingsGoalDto.GoalProgressResponse;
import com.syfe.personalfinance.dto.SavingsGoalDto.GoalResponse;
import com.syfe.personalfinance.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    // Constructor injection only
    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody SavingsGoalDto.CreateGoalRequest request) {
        GoalResponse responseData = savingsGoalService.createGoal(request);
        ApiResponse<GoalResponse> response = ApiResponse.<GoalResponse>builder()
                .success(true)
                .message("Savings goal created successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalProgressResponse>>> getAllGoalsProgress() {
        List<GoalProgressResponse> responseData = savingsGoalService.getAllGoalsProgress();
        ApiResponse<List<GoalProgressResponse>> response = ApiResponse.<List<GoalProgressResponse>>builder()
                .success(true)
                .message("Savings goals progress retrieved successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> getGoalProgress(@PathVariable Long id) {
        GoalProgressResponse responseData = savingsGoalService.getGoalProgress(id);
        ApiResponse<GoalProgressResponse> response = ApiResponse.<GoalProgressResponse>builder()
                .success(true)
                .message("Savings goal progress retrieved successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalDto.UpdateGoalRequest request) {

        GoalResponse responseData = savingsGoalService.updateGoal(id, request);
        ApiResponse<GoalResponse> response = ApiResponse.<GoalResponse>builder()
                .success(true)
                .message("Savings goal updated successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        savingsGoalService.deleteGoal(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Savings goal deleted successfully")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
