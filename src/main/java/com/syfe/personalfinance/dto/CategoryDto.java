package com.syfe.personalfinance.dto;

import com.syfe.personalfinance.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class CategoryDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        private String name;

        @NotNull(message = "Category type (INCOME or EXPENSE) is required")
        private CategoryType type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResponse {
        private String name;
        private CategoryType type;
        private boolean isCustom;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryListResponse {
        private List<CategoryResponse> categories;
    }
}
