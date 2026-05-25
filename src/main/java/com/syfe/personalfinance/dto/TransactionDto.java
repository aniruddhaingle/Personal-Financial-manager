package com.syfe.personalfinance.dto;

import com.syfe.personalfinance.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

        @NotBlank(message = "Category name is required")
        private String category;

        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateTransactionRequest {
        @Positive(message = "Amount must be a positive number")
        private BigDecimal amount;

        private String category;

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
        private String category;
        private String description;
        private CategoryType type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionListResponse {
        private List<TransactionResponse> transactions;
    }
}
