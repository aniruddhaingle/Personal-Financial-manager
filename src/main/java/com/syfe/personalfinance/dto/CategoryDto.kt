package com.syfe.personalfinance.dto

import com.syfe.personalfinance.enums.CategoryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class CategoryDto {

    data class CreateCategoryRequest(
        @field:NotBlank(message = "Category name is required")
        val name: String = "",

        @field:NotNull(message = "Category type (INCOME or EXPENSE) is required")
        val type: CategoryType? = null
    )

    data class CategoryResponse(
        val name: String = "",
        val type: CategoryType? = null,
        val isCustom: Boolean = false
    )

    data class CategoryListResponse(
        val categories: List<CategoryResponse> = emptyList()
    )
}
