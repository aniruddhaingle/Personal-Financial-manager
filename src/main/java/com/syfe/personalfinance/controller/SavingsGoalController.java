package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.dto.SavingsGoalDto;
import com.syfe.personalfinance.dto.SavingsGoalDto.GoalProgressResponse;
import com.syfe.personalfinance.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<GoalProgressResponse> createGoal(@Valid @RequestBody SavingsGoalDto.CreateGoalRequest request) {
        GoalProgressResponse responseData = savingsGoalService.createGoal(request);
        return new ResponseEntity<>(responseData, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<SavingsGoalDto.GoalListResponse> getAllGoalsProgress() {
        List<GoalProgressResponse> responseData = savingsGoalService.getAllGoalsProgress();
        SavingsGoalDto.GoalListResponse response = SavingsGoalDto.GoalListResponse.builder()
                .goals(responseData)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalProgressResponse> getGoalProgress(@PathVariable Long id) {
        GoalProgressResponse responseData = savingsGoalService.getGoalProgress(id);
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalProgressResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalDto.UpdateGoalRequest request) {

        GoalProgressResponse responseData = savingsGoalService.updateGoal(id, request);
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AuthDto.SimpleMessageResponse> deleteGoal(@PathVariable Long id) {
        savingsGoalService.deleteGoal(id);
        AuthDto.SimpleMessageResponse response = AuthDto.SimpleMessageResponse.builder()
                .message("Goal deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
