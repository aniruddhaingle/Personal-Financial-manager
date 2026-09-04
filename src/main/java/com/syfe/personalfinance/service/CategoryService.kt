package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.CategoryDto

/**
 * Service managing user categories and system-defined default categories.
 */
interface CategoryService {
    /**
     * Creates a custom category scoped to the authenticated user.
     *
     * @param request the category details (name, type)
     * @return the created category with custom indicator
     * @throws com.syfe.personalfinance.exception.ConflictException if category with same name already exists
     */
    fun createCategory(request: CategoryDto.CreateCategoryRequest): CategoryDto.CategoryResponse

    /**
     * Retrieves all categories accessible to the current user (global defaults + user's custom categories).
     *
     * @return list of available categories
     */
    fun getAllCategories(): List<CategoryDto.CategoryResponse>

    /**
     * Deletes a custom category belonging to the current user.
     *
     * @param name name of the custom category to delete
     * @throws com.syfe.personalfinance.exception.ForbiddenException if attempting to delete a default system category
     * @throws com.syfe.personalfinance.exception.BadRequestException if category is currently referenced by transactions
     * @throws com.syfe.personalfinance.exception.ResourceNotFoundException if category does not exist
     */
    fun deleteCategory(name: String)
}

