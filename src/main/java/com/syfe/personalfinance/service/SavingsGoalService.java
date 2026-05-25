package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.SavingsGoalDto;

import java.util.List;

public interface SavingsGoalService {
    SavingsGoalDto.GoalResponse createGoal(SavingsGoalDto.CreateGoalRequest request);
    List<SavingsGoalDto.GoalProgressResponse> getAllGoalsProgress();
    SavingsGoalDto.GoalProgressResponse getGoalProgress(Long id);
    SavingsGoalDto.GoalResponse updateGoal(Long id, SavingsGoalDto.UpdateGoalRequest request);
    void deleteGoal(Long id);
}
