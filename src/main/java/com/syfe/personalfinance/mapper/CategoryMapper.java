package com.syfe.personalfinance.mapper;

import com.syfe.personalfinance.dto.CategoryDto;
import com.syfe.personalfinance.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto.CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .isDefault(category.isDefault())
                .build();
    }

    public Category toEntity(CategoryDto.CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }
        return Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isDefault(false) // Custom categories are never default
                .build();
    }
}
