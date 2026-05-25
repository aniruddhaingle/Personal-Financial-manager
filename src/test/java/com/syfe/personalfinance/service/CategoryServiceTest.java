package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.CategoryDto;
import com.syfe.personalfinance.dto.CategoryDto.CategoryResponse;
import com.syfe.personalfinance.entity.Category;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.exception.BadRequestException;
import com.syfe.personalfinance.exception.ConflictException;
import com.syfe.personalfinance.exception.ForbiddenException;
import com.syfe.personalfinance.mapper.CategoryMapper;
import com.syfe.personalfinance.repository.CategoryRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private UserService userService;

    private CategoryService categoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, transactionRepository, categoryMapper, userService);
        testUser = User.builder().id(100L).username("user@syfe.com").build();
    }

    @Test
    void createCategory_Success() {
        CategoryDto.CreateCategoryRequest request = CategoryDto.CreateCategoryRequest.builder()
                .name("Freelance")
                .type(CategoryType.INCOME)
                .build();

        Category categoryEntity = Category.builder()
                .name("Freelance")
                .type(CategoryType.INCOME)
                .user(testUser)
                .isDefault(false)
                .build();

        CategoryResponse expectedResponse = CategoryResponse.builder()
                .name("Freelance")
                .type(CategoryType.INCOME)
                .isCustom(true)
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Freelance", 100L)).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(categoryEntity);
        when(categoryRepository.save(any(Category.class))).thenReturn(categoryEntity);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(expectedResponse);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Freelance", response.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ConflictingWithDefault_ThrowsException() {
        CategoryDto.CreateCategoryRequest request = CategoryDto.CreateCategoryRequest.builder()
                .name("Salary")
                .type(CategoryType.INCOME)
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("Salary")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_DefaultCategory_ThrowsException() {
        Category defaultCategory = Category.builder()
                .id(1L)
                .name("Food")
                .isDefault(true)
                .user(null)
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Food", 100L)).thenReturn(Optional.of(defaultCategory));

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("Food"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_UnauthorizedUser_ThrowsException() {
        User otherUser = User.builder().id(200L).username("other@syfe.com").build();
        Category customCategory = Category.builder()
                .id(5L)
                .name("Snacks")
                .isDefault(false)
                .user(otherUser)
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Snacks", 100L)).thenReturn(Optional.of(customCategory));

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory("Snacks"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_WithLinkedTransactions_ThrowsException() {
        Category customCategory = Category.builder()
                .id(5L)
                .name("Snacks")
                .isDefault(false)
                .user(testUser)
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Snacks", 100L)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(5L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory("Snacks"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
