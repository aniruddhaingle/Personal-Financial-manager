package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.TransactionDto
import com.syfe.personalfinance.dto.TransactionDto.TransactionResponse
import com.syfe.personalfinance.entity.Category
import com.syfe.personalfinance.entity.Transaction
import com.syfe.personalfinance.entity.User
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.mapper.TransactionMapper
import com.syfe.personalfinance.repository.CategoryRepository
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.impl.TransactionServiceImpl
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
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TransactionServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var transactionMapper: TransactionMapper

    @Mock
    private lateinit var userService: UserService

    private lateinit var transactionService: TransactionService

    private lateinit var testUser: User
    private lateinit var testCategory: Category

    @BeforeEach
    fun setUp() {
        transactionService = TransactionServiceImpl(transactionRepository, categoryRepository, transactionMapper, userService)
        testUser = User(id = 100L, username = "user@syfe.com")
        testCategory = Category(id = 10L, name = "Food", type = CategoryType.EXPENSE)
    }

    @Test
    fun createTransaction_Success() {
        val date = LocalDate.now().minusDays(1)
        val request = TransactionDto.CreateTransactionRequest(
            amount = BigDecimal("150.00"),
            date = date,
            category = "Food",
            description = "Groceries"
        )

        val transactionEntity = Transaction(
            amount = BigDecimal("150.00"),
            date = date,
            description = "Groceries",
            category = testCategory,
            user = testUser
        )

        val expectedResponse = TransactionResponse(
            id = 50L,
            amount = BigDecimal("150.00"),
            date = date,
            category = "Food",
            description = "Groceries",
            type = CategoryType.EXPENSE
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Food", 100L)).thenReturn(Optional.of(testCategory))
        whenever(transactionMapper.toEntity(request)).thenReturn(transactionEntity)
        whenever(transactionRepository.save(any<Transaction>())).thenReturn(transactionEntity)
        whenever(transactionMapper.toResponse(any<Transaction>())).thenReturn(expectedResponse)

        val response = transactionService.createTransaction(request)

        assertNotNull(response)
        assertEquals(BigDecimal("150.00"), response.amount)
        assertEquals("Groceries", response.description)
        verify(transactionRepository, times(1)).save(any<Transaction>())
    }

    @Test
    fun createTransaction_FutureDate_ThrowsException() {
        val futureDate = LocalDate.now().plusDays(1)
        val request = TransactionDto.CreateTransactionRequest(
            amount = BigDecimal("150.00"),
            date = futureDate,
            category = "Food"
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)

        assertThrows(BadRequestException::class.java) { transactionService.createTransaction(request) }
        verify(transactionRepository, never()).save(any<Transaction>())
    }

    @Test
    fun updateTransaction_Success_KeepsDateSame() {
        val existingDate = LocalDate.now().minusDays(5)
        val existingTransaction = Transaction(
            id = 50L,
            amount = BigDecimal("100.00"),
            date = existingDate,
            description = "Old Desc",
            category = testCategory,
            user = testUser
        )

        val request = TransactionDto.UpdateTransactionRequest(
            amount = BigDecimal("120.00"),
            category = "Food",
            description = "New Desc"
        )

        val expectedResponse = TransactionResponse(
            id = 50L,
            amount = BigDecimal("120.00"),
            date = existingDate,
            description = "New Desc"
        )

        whenever(userService.getAuthenticatedUserEntity()).thenReturn(testUser)
        whenever(transactionRepository.findByIdAndUserId(50L, 100L)).thenReturn(Optional.of(existingTransaction))
        whenever(categoryRepository.findByNameIgnoreCaseAndUserAvailable("Food", 100L)).thenReturn(Optional.of(testCategory))
        whenever(transactionRepository.save(any<Transaction>())).thenAnswer { it.arguments[0] }
        whenever(transactionMapper.toResponse(any<Transaction>())).thenReturn(expectedResponse)

        val response = transactionService.updateTransaction(50L, request)

        assertNotNull(response)
        assertEquals(BigDecimal("120.00"), response.amount)
        assertEquals(existingDate, response.date)
        assertEquals("New Desc", response.description)
    }
}
