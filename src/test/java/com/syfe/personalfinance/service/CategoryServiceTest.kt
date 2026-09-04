package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.CategoryDto
import com.syfe.personalfinance.dto.CategoryDto.CategoryResponse
import com.syfe.personalfinance.entity.Category
import com.syfe.personalfinance.entity.User
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.exception.ConflictException
import com.syfe.personalfinance.exception.ForbiddenException
import com.syfe.personalfinance.mapper.CategoryMapper
import com.syfe.personalfinance.repository.CategoryRepository
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.impl.CategoryServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var categoryMapper: CategoryMapper

    @Mock
    private lateinit var userService: UserService

    private lateinit var categoryService: CategoryService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        categoryService = CategoryServiceImpl(categoryRepository, transactionRepository, categoryMapper, userService)
        testUser = User(id = 100L, username = "user@syfe.com")
    }

    @Test
    fun createCategory_Success() {
        val request = CategoryDto.CreateCategoryRequest(
            name = "Freelance",
            type = CategoryType.INCOME
        )

        val categoryEntity = Category(
            name = "Freelance",
            type = CategoryType.INCOME,
            user = testUser,
            isDefault = false
        )

        val expectedResponse = CategoryResponse(
            name = "Freelance",
            type = CategoryType.INCOME,
            isCustom = true
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("Freelance")).thenReturn(false)
        whenever(categoryRepository.existsByNameIgnoreCaseAndUserId("Freelance", 100L)).thenReturn(false)
        whenever(categoryMapper.toEntity(request)).thenReturn(categoryEntity)
        whenever(categoryRepository.save(any<Category>())).thenReturn(categoryEntity)
        whenever(categoryMapper.toResponse(any<Category>())).thenReturn(expectedResponse)

        val response = categoryService.createCategory(request)

        assertNotNull(response)
        assertEquals("Freelance", response.name)
        verify(categoryRepository, times(1)).save(any<Category>())
    }

    @Test
    fun createCategory_ConflictingWithDefault_ThrowsException() {
        val request = CategoryDto.CreateCategoryRequest(
            name = "Salary",
            type = CategoryType.INCOME
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("Salary")).thenReturn(true)

        assertThrows(ConflictException::class.java) { categoryService.createCategory(request) }
        verify(categoryRepository, never()).save(any<Category>())
    }

    @Test
    fun deleteCategory_DefaultCategory_ThrowsException() {
        val defaultCategory = Category(
            id = 1L,
            name = "Food",
            type = CategoryType.EXPENSE,
            isDefault = true,
            user = null
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Food", 100L)).thenReturn(Optional.of(defaultCategory))

        assertThrows(BadRequestException::class.java) { categoryService.deleteCategory("Food") }
        verify(categoryRepository, never()).delete(any<Category>())
    }

    @Test
    fun deleteCategory_UnauthorizedUser_ThrowsException() {
        val otherUser = User(id = 200L, username = "other@syfe.com")
        val customCategory = Category(
            id = 5L,
            name = "Snacks",
            type = CategoryType.EXPENSE,
            isDefault = false,
            user = otherUser
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Snacks", 100L)).thenReturn(Optional.of(customCategory))

        assertThrows(ForbiddenException::class.java) { categoryService.deleteCategory("Snacks") }
        verify(categoryRepository, never()).delete(any<Category>())
    }

    @Test
    fun deleteCategory_WithLinkedTransactions_ThrowsException() {
        val customCategory = Category(
            id = 5L,
            name = "Snacks",
            type = CategoryType.EXPENSE,
            isDefault = false,
            user = testUser
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Snacks", 100L)).thenReturn(Optional.of(customCategory))
        whenever(transactionRepository.existsByCategoryId(5L)).thenReturn(true)

        assertThrows(ConflictException::class.java) { categoryService.deleteCategory("Snacks") }
        verify(categoryRepository, never()).delete(any<Category>())
    }
}
