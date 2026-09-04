package com.syfe.personalfinance.service.impl

import com.syfe.personalfinance.dto.CategoryDto
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.exception.ConflictException
import com.syfe.personalfinance.exception.ForbiddenException
import com.syfe.personalfinance.exception.ResourceNotFoundException
import com.syfe.personalfinance.mapper.CategoryMapper
import com.syfe.personalfinance.repository.CategoryRepository
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.CategoryService
import com.syfe.personalfinance.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryMapper: CategoryMapper,
    private val userService: UserService
) : CategoryService {

    private val log = LoggerFactory.getLogger(CategoryServiceImpl::class.java)

    @Transactional
    override fun createCategory(request: CategoryDto.CreateCategoryRequest): CategoryDto.CategoryResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        val categoryName = request.name.trim()
        log.info("Creating custom category '{}' for user ID: {}", categoryName, currentUser.id)

        if (categoryRepository.existsByNameIgnoreCaseAndUserIsNull(categoryName)) {
            log.warn("Category creation failed: Name '{}' is a reserved default category", categoryName)
            throw ConflictException("A default category with this name already exists")
        }

        if (categoryRepository.existsByNameIgnoreCaseAndUserId(categoryName, currentUser.id!!)) {
            log.warn("Category creation failed: Custom category '{}' already exists for user ID: {}", categoryName, currentUser.id)
            throw ConflictException("Custom category name must be unique per user")
        }

        val category = categoryMapper.toEntity(request) ?: throw IllegalStateException("Failed to map category entity")
        category.name = categoryName
        category.user = currentUser
        category.isDefault = false

        val savedCategory = categoryRepository.save(category)
        log.info("Custom category created successfully with ID: {}", savedCategory.id)

        return categoryMapper.toResponse(savedCategory) ?: throw IllegalStateException("Failed to map category response")
    }

    @Transactional(readOnly = true)
    override fun getAllCategories(): List<CategoryDto.CategoryResponse> {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.debug("Fetching all available categories for user ID: {}", currentUser.id)

        val categories = categoryRepository.findAllAvailableToUser(currentUser.id!!)

        return categories.mapNotNull { categoryMapper.toResponse(it) }
    }

    @Transactional
    override fun deleteCategory(name: String) {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Processing deletion request for category '{}' by user ID: {}", name, currentUser.id)

        val category = categoryRepository.findByNameIgnoreCaseAndUserAvailable(name, currentUser.id!!)
            .orElseThrow {
                log.warn("Category deletion failed: '{}' not found", name)
                ResourceNotFoundException("Category not found")
            }

        if (category.isDefault || category.user == null) {
            log.warn("Category deletion failed: '{}' is a global default category", name)
            throw BadRequestException("Default categories cannot be deleted")
        }

        if (category.user?.id != currentUser.id) {
            log.warn("Category deletion failed: Custom category '{}' does not belong to user ID: {}", name, currentUser.id)
            throw ForbiddenException("Access denied: You do not own this category")
        }

        if (transactionRepository.existsByCategoryId(category.id!!)) {
            log.warn("Category deletion failed: Custom category '{}' is currently linked to active transactions", name)
            throw ConflictException("Cannot delete category as it is currently used in transactions")
        }

        categoryRepository.delete(category)
        log.info("Category '{}' deleted successfully", name)
    }
}
