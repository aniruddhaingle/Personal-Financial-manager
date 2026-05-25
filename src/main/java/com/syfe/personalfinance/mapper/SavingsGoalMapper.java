package com.syfe.personalfinance.mapper;

import com.syfe.personalfinance.dto.SavingsGoalDto;
import com.syfe.personalfinance.entity.SavingsGoal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SavingsGoalMapper {

    public SavingsGoalDto.GoalResponse toResponse(SavingsGoal goal) {
        if (goal == null) {
            return null;
        }
        return SavingsGoalDto.GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .build();
    }

    public SavingsGoalDto.GoalProgressResponse toProgressResponse(SavingsGoal goal,
                                                                   BigDecimal currentProgress,
                                                                   BigDecimal progressPercentage,
                                                                   BigDecimal remainingAmount) {
        if (goal == null) {
            return null;
        }
        return SavingsGoalDto.GoalProgressResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .currentProgress(currentProgress)
                .progressPercentage(progressPercentage)
                .remainingAmount(remainingAmount)
                .build();
    }

    public SavingsGoal toEntity(SavingsGoalDto.CreateGoalRequest request) {
        if (request == null) {
            return null;
        }
        return SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .build();
    }
}
