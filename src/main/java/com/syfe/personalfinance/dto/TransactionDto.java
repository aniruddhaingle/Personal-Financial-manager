package com.syfe.personalfinance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateTransactionRequest {
        @NotNull(message = "Transaction amount is required")
        @Positive(message = "Amount must be a positive number")
        private BigDecimal amount;

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        private LocalDate date;

        @NotNull(message = "Category identifier is required")
        private Long categoryId;

        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateTransactionRequest {
        @NotNull(message = "Transaction amount is required")
        @Positive(message = "Amount must be a positive number")
        private BigDecimal amount;

        @NotNull(message = "Category identifier is required")
        private Long categoryId;

        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionResponse {
        private Long id;
        private BigDecimal amount;
        private LocalDate date;
        private String description;
        private CategoryDto.CategoryResponse category;
    }
}
