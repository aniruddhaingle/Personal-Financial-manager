package com.syfe.personalfinance.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        @NotNull(message = "Start date is required")
        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @NotNull(message = "Target date is required")
        @Future(message = "Target date must be in the future")
        private LocalDate targetDate; // Matches assignment targetDate
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateGoalRequest {
        @NotBlank(message = "Goal name is required")
        private String goalName;

        @NotNull(message = "Target amount is required")
        @Positive(message = "Target amount must be a positive number")
        private BigDecimal targetAmount;

        @NotNull(message = "Start date is required")
        @PastOrPresent(message = "Start date cannot be in the future")
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
    public static class GoalResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate startDate;
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
        private BigDecimal progressPercentage;
        private BigDecimal remainingAmount;
    }
}
