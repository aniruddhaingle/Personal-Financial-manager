package com.syfe.personalfinance.controller

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.dto.CategoryDto
import com.syfe.personalfinance.service.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller managing default and user-defined financial categories.
 */
@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * Creates a new custom category for the authenticated user.
     */
    @PostMapping
    fun createCategory(@Valid @RequestBody request: CategoryDto.CreateCategoryRequest): ResponseEntity<CategoryDto.CategoryResponse> {
        val responseData = categoryService.createCategory(request)
        return ResponseEntity(responseData, HttpStatus.CREATED)
    }

    /**
     * Retrieves all default system categories alongside user custom categories.
     */
    @GetMapping
    fun getAllCategories(): ResponseEntity<CategoryDto.CategoryListResponse> {
        val responseData = categoryService.getAllCategories()
        val response = CategoryDto.CategoryListResponse(
            categories = responseData
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Deletes a custom category by its name, verifying user ownership and reference integrity.
     */
    @DeleteMapping("/{name}")
    fun deleteCategory(@PathVariable name: String): ResponseEntity<AuthDto.SimpleMessageResponse> {
        categoryService.deleteCategory(name)
        val response = AuthDto.SimpleMessageResponse(
            message = "Category deleted successfully"
        )
        return ResponseEntity.ok(response)
    }
}
