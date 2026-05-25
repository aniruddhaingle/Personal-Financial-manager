package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.dto.CategoryDto;
import com.syfe.personalfinance.dto.CategoryDto.CategoryResponse;
import com.syfe.personalfinance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryDto.CreateCategoryRequest request) {
        CategoryResponse responseData = categoryService.createCategory(request);
        return new ResponseEntity<>(responseData, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CategoryDto.CategoryListResponse> getAllCategories() {
        List<CategoryResponse> responseData = categoryService.getAllCategories();
        CategoryDto.CategoryListResponse response = CategoryDto.CategoryListResponse.builder()
                .categories(responseData)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<AuthDto.SimpleMessageResponse> deleteCategory(@PathVariable String name) {
        categoryService.deleteCategory(name);
        AuthDto.SimpleMessageResponse response = AuthDto.SimpleMessageResponse.builder()
                .message("Category deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
