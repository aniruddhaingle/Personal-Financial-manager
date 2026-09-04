package com.syfe.personalfinance.mapper

import com.syfe.personalfinance.dto.CategoryDto
import com.syfe.personalfinance.entity.Category
import com.syfe.personalfinance.enums.CategoryType
import org.springframework.stereotype.Component

@Component
class CategoryMapper {

    fun toResponse(category: Category?): CategoryDto.CategoryResponse? {
        if (category == null) {
            return null
        }
        return CategoryDto.CategoryResponse(
            name = category.name,
            type = category.type,
            isCustom = !category.isDefault
        )
    }

    fun toEntity(request: CategoryDto.CreateCategoryRequest?): Category? {
        if (request == null) {
            return null
        }
        return Category(
            name = request.name,
            type = request.type ?: CategoryType.EXPENSE,
            isDefault = false
        )
    }
}
