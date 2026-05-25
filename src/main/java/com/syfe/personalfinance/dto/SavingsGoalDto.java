package com.syfe.personalfinance.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SavingsGoalDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateGoalRequest {
        @NotBlank(message = "Goal name is required")
        private String goalName;

        @NotNull(message = "Target amount is required")
        @Positive(message = "Target amount must be a positive number")
        private BigDecimal targetAmount;

        private LocalDate startDate;

        @NotNull(message = "Target date is required")
        @Future(message = "Target date must be in the future")
        private LocalDate targetDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateGoalRequest {
        private String goalName;

        @Positive(message = "Target amount must be a positive number")
        private BigDecimal targetAmount;

        private LocalDate startDate;

        @Future(message = "Target date must be in the future")
        private LocalDate targetDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalProgressResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate startDate;
        private LocalDate targetDate;
        private BigDecimal currentProgress;
        private Double progressPercentage;
        private BigDecimal remainingAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalListResponse {
        private List<GoalProgressResponse> goals;
    }
}
