package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.CategoryDto;
import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.dto.TransactionDto.TransactionResponse;
import com.syfe.personalfinance.entity.Category;
import com.syfe.personalfinance.entity.Transaction;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.exception.BadRequestException;
import com.syfe.personalfinance.mapper.TransactionMapper;
import com.syfe.personalfinance.repository.CategoryRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private UserService userService;

    private TransactionService transactionService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository, categoryRepository, transactionMapper, userService);
        testUser = User.builder().id(100L).username("user@syfe.com").build();
        testCategory = Category.builder().id(10L).name("Food").type(CategoryType.EXPENSE).build();
    }

    @Test
    void createTransaction_Success() {
        LocalDate date = LocalDate.now().minusDays(1);
        TransactionDto.CreateTransactionRequest request = TransactionDto.CreateTransactionRequest.builder()
                .amount(new BigDecimal("150.00"))
                .date(date)
                .categoryId(10L)
                .description("Groceries")
                .build();

        Transaction transactionEntity = Transaction.builder()
                .amount(new BigDecimal("150.00"))
                .date(date)
                .description("Groceries")
                .category(testCategory)
                .user(testUser)
                .build();

        TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(50L)
                .amount(new BigDecimal("150.00"))
                .date(date)
                .description("Groceries")
                .category(CategoryDto.CategoryResponse.builder().id(10L).name("Food").build())
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUserAvailable(10L, 100L)).thenReturn(Optional.of(testCategory));
        when(transactionMapper.toEntity(request)).thenReturn(transactionEntity);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionEntity);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(expectedResponse);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals("Groceries", response.getDescription());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_FutureDate_ThrowsException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        TransactionDto.CreateTransactionRequest request = TransactionDto.CreateTransactionRequest.builder()
                .amount(new BigDecimal("150.00"))
                .date(futureDate)
                .categoryId(10L)
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_Success_KeepsDateSame() {
        Transaction existingTransaction = Transaction.builder()
                .id(50L)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now().minusDays(5)) // Date is set
                .description("Old Desc")
                .category(testCategory)
                .user(testUser)
                .build();

        TransactionDto.UpdateTransactionRequest request = TransactionDto.UpdateTransactionRequest.builder()
                .amount(new BigDecimal("120.00"))
                .categoryId(10L)
                .description("New Desc")
                .build();

        TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(50L)
                .amount(new BigDecimal("120.00"))
                .date(LocalDate.now().minusDays(5)) // Date unchanged
                .description("New Desc")
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(transactionRepository.findByIdAndUserId(50L, 100L)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserAvailable(10L, 100L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(expectedResponse);

        TransactionResponse response = transactionService.updateTransaction(50L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("120.00"), response.getAmount());
        assertEquals(LocalDate.now().minusDays(5), response.getDate()); // Enforced date constraint
        assertEquals("New Desc", response.getDescription());
    }
}
