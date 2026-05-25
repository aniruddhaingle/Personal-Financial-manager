package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto.CategoryResponse createCategory(CategoryDto.CreateCategoryRequest request);
    List<CategoryDto.CategoryResponse> getAllCategories();
    void deleteCategory(Long id);
}
