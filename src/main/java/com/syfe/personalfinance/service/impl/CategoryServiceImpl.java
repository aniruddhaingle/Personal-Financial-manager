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

        if (categoryRepository.existsByNameIgnoreCaseAndUserIsNull(categoryName)) {
            log.warn("Category creation failed: Name '{}' is a reserved default category", categoryName);
            throw new ConflictException("A default category with this name already exists");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndUserId(categoryName, currentUser.getId())) {
            log.warn("Category creation failed: Custom category '{}' already exists for user ID: {}", categoryName, currentUser.getId());
            throw new ConflictException("Custom category name must be unique per user");
        }

        Category category = categoryMapper.toEntity(request);
        category.setName(categoryName);
        category.setUser(currentUser);
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
    public void deleteCategory(String name) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Processing deletion request for category '{}' by user ID: {}", name, currentUser.getId());

        Category category = categoryRepository.findByNameIgnoreCaseAndUserAvailable(name, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Category deletion failed: '{}' not found", name);
                    return new ResourceNotFoundException("Category not found");
                });

        if (category.isDefault() || category.getUser() == null) {
            log.warn("Category deletion failed: '{}' is a global default category", name);
            throw new BadRequestException("Default categories cannot be deleted");
        }

        if (!category.getUser().getId().equals(currentUser.getId())) {
            log.warn("Category deletion failed: Custom category '{}' does not belong to user ID: {}", name, currentUser.getId());
            throw new ForbiddenException("Access denied: You do not own this category");
        }

        if (transactionRepository.existsByCategoryId(category.getId())) {
            log.warn("Category deletion failed: Custom category '{}' is currently linked to active transactions", name);
            throw new ConflictException("Cannot delete category as it is currently used in transactions");
        }

        categoryRepository.delete(category);
        log.info("Category '{}' deleted successfully", name);
    }
}
