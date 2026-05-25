package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.SavingsGoalDto;
import com.syfe.personalfinance.dto.SavingsGoalDto.GoalProgressResponse;
import com.syfe.personalfinance.entity.SavingsGoal;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.exception.BadRequestException;
import com.syfe.personalfinance.mapper.SavingsGoalMapper;
import com.syfe.personalfinance.repository.SavingsGoalRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.impl.SavingsGoalServiceImpl;
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
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SavingsGoalMapper savingsGoalMapper;

    @Mock
    private UserService userService;

    private SavingsGoalService savingsGoalService;

    private User testUser;

    @BeforeEach
    void setUp() {
        savingsGoalService = new SavingsGoalServiceImpl(savingsGoalRepository, transactionRepository, savingsGoalMapper, userService);
        testUser = User.builder().id(100L).username("user@syfe.com").build();
    }

    @Test
    void createGoal_Success() {
        SavingsGoalDto.CreateGoalRequest request = SavingsGoalDto.CreateGoalRequest.builder()
                .goalName("Car fund")
                .targetAmount(new BigDecimal("10000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .build();

        SavingsGoal goalEntity = SavingsGoal.builder()
                .goalName("Car fund")
                .targetAmount(new BigDecimal("10000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusYears(1))
                .user(testUser)
                .build();

        GoalProgressResponse expectedResponse = GoalProgressResponse.builder()
                .id(1L)
                .goalName("Car fund")
                .targetAmount(new BigDecimal("10000.00"))
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(savingsGoalMapper.toEntity(request)).thenReturn(goalEntity);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goalEntity);
        when(savingsGoalMapper.toProgressResponse(any(SavingsGoal.class), any(), any(), any())).thenReturn(expectedResponse);

        GoalProgressResponse response = savingsGoalService.createGoal(request);

        assertNotNull(response);
        assertEquals("Car fund", response.getGoalName());
        verify(savingsGoalRepository, times(1)).save(any(SavingsGoal.class));
    }

    @Test
    void createGoal_TargetBeforeStart_ThrowsException() {
        SavingsGoalDto.CreateGoalRequest request = SavingsGoalDto.CreateGoalRequest.builder()
                .goalName("Car fund")
                .targetAmount(new BigDecimal("10000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().minusDays(5))
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> savingsGoalService.createGoal(request));
        verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
    }

    @Test
    void getGoalProgress_SuccessCalculations() {
        LocalDate startDate = LocalDate.now().minusMonths(3);
        SavingsGoal goal = SavingsGoal.builder()
                .id(1L)
                .goalName("Car fund")
                .targetAmount(new BigDecimal("10000.00"))
                .startDate(startDate)
                .targetDate(LocalDate.now().plusYears(1))
                .user(testUser)
                .build();

        GoalProgressResponse expectedProgress = GoalProgressResponse.builder()
                .id(1L)
                .goalName("Car fund")
                .targetAmount(new BigDecimal("10000.00"))
                .currentProgress(new BigDecimal("3000.00"))
                .progressPercentage(new BigDecimal("30.00"))
                .remainingAmount(new BigDecimal("7000.00"))
                .build();

        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
        when(savingsGoalRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(goal));

        when(transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(100L, CategoryType.INCOME, startDate))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(100L, CategoryType.EXPENSE, startDate))
                .thenReturn(new BigDecimal("2000.00"));

        when(savingsGoalMapper.toProgressResponse(eq(goal), any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(expectedProgress);

        GoalProgressResponse progress = savingsGoalService.getGoalProgress(1L);

        assertNotNull(progress);
        assertEquals(new BigDecimal("3000.00"), progress.getCurrentProgress());
        assertEquals(new BigDecimal("30.00"), progress.getProgressPercentage());
        assertEquals(new BigDecimal("7000.00"), progress.getRemainingAmount());
    }
}
