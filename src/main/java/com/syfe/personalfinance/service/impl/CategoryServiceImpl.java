package com.syfe.personalfinance.service.impl;

import com.syfe.personalfinance.dto.CategoryDto;
import com.syfe.personalfinance.entity.Category;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.exception.BadRequestException;
import com.syfe.personalfinance.exception.ConflictException;
import com.syfe.personalfinance.exception.ForbiddenException;
import com.syfe.personalfinance.exception.ResourceNotFoundException;
import com.syfe.personalfinance.mapper.CategoryMapper;
import com.syfe.personalfinance.repository.CategoryRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.CategoryService;
import com.syfe.personalfinance.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    // Constructor injection only
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               TransactionRepository transactionRepository,
                               CategoryMapper categoryMapper,
                               UserService userService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public CategoryDto.CategoryResponse createCategory(CategoryDto.CreateCategoryRequest request) {
        User currentUser = userService.getAuthenticatedUserEntity();
        String categoryName = request.getName().trim();
        log.info("Creating custom category '{}' for user ID: {}", categoryName, currentUser.getId());

        // Check if category name conflicts with any global default category (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCaseAndUserIsNull(categoryName)) {
            log.warn("Category creation failed: Name '{}' is a reserved default category", categoryName);
            throw new ConflictException("A default category with this name already exists");
        }

        // Check if category name conflicts with user's existing custom categories (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCaseAndUserId(categoryName, currentUser.getId())) {
            log.warn("Category creation failed: Custom category '{}' already exists for user ID: {}", categoryName, currentUser.getId());
            throw new ConflictException("Custom category name must be unique per user");
        }

        Category category = categoryMapper.toEntity(request);
        category.setName(categoryName);
        category.setUser(currentUser); // Associate custom category with active authenticated user
        category.setDefault(false);

        Category savedCategory = categoryRepository.save(category);
        log.info("Custom category created successfully with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto.CategoryResponse> getAllCategories() {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.debug("Fetching all available categories for user ID: {}", currentUser.getId());

        List<Category> categories = categoryRepository.findAllAvailableToUser(currentUser.getId());

        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Processing deletion request for category ID: {} by user ID: {}", id, currentUser.getId());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category deletion failed: ID {} not found", id);
                    return new ResourceNotFoundException("Category not found");
                });

        // 1. Prevent deletion of default categories
        if (category.isDefault() || category.getUser() == null) {
            log.warn("Category deletion failed: ID {} is a global default category", id);
            throw new BadRequestException("Default categories cannot be deleted");
        }

        // 2. Prevent unauthorized deletion of other users' custom categories (Ownership check)
        if (!category.getUser().getId().equals(currentUser.getId())) {
            log.warn("Category deletion failed: Custom category ID {} does not belong to user ID: {}", id, currentUser.getId());
            throw new ForbiddenException("Access denied: You do not own this category");
        }

        // 3. Prevent deletion of categories already used in transactions
        if (transactionRepository.existsByCategoryId(id)) {
            log.warn("Category deletion failed: Custom category ID {} is currently linked to active transactions", id);
            throw new ConflictException("Cannot delete category as it is currently used in transactions");
        }

        categoryRepository.delete(category);
        log.info("Category ID: {} deleted successfully", id);
    }
}
