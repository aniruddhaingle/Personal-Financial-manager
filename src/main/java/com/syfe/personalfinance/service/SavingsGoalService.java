package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.SavingsGoalDto;

import java.util.List;

public interface SavingsGoalService {
    SavingsGoalDto.GoalProgressResponse createGoal(SavingsGoalDto.CreateGoalRequest request);
    List<SavingsGoalDto.GoalProgressResponse> getAllGoalsProgress();
    SavingsGoalDto.GoalProgressResponse getGoalProgress(Long id);
    SavingsGoalDto.GoalProgressResponse updateGoal(Long id, SavingsGoalDto.UpdateGoalRequest request);
    void deleteGoal(Long id);
}
