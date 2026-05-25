package com.syfe.personalfinance.service.impl;

import com.syfe.personalfinance.dto.SavingsGoalDto;
import com.syfe.personalfinance.entity.SavingsGoal;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.exception.BadRequestException;
import com.syfe.personalfinance.exception.ResourceNotFoundException;
import com.syfe.personalfinance.mapper.SavingsGoalMapper;
import com.syfe.personalfinance.repository.SavingsGoalRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.SavingsGoalService;
import com.syfe.personalfinance.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalMapper savingsGoalMapper;
    private final UserService userService;

    // Constructor injection only
    public SavingsGoalServiceImpl(SavingsGoalRepository savingsGoalRepository,
                                  TransactionRepository transactionRepository,
                                  SavingsGoalMapper savingsGoalMapper,
                                  UserService userService) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
        this.savingsGoalMapper = savingsGoalMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public SavingsGoalDto.GoalResponse createGoal(SavingsGoalDto.CreateGoalRequest request) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Creating new savings goal '{}' for user ID: {}", request.getGoalName(), currentUser.getId());

        // Validate goal timeline
        if (request.getTargetDate().isBefore(request.getStartDate())) {
            log.warn("Goal creation failed: Target date {} is before start date {}", request.getTargetDate(), request.getStartDate());
            throw new BadRequestException("Target date cannot be before the start date");
        }

        SavingsGoal savingsGoal = savingsGoalMapper.toEntity(request);
        savingsGoal.setUser(currentUser);

        SavingsGoal savedGoal = savingsGoalRepository.save(savingsGoal);
        log.info("Savings goal created successfully with ID: {}", savedGoal.getId());

        return savingsGoalMapper.toResponse(savedGoal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalDto.GoalProgressResponse> getAllGoalsProgress() {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.debug("Calculating dynamic savings progress for all goals of user ID: {}", currentUser.getId());

        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(currentUser.getId());

        return goals.stream()
                .map(this::calculateGoalProgress)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalDto.GoalProgressResponse getGoalProgress(Long id) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.debug("Calculating dynamic savings progress for goal ID: {} and user ID: {}", id, currentUser.getId());

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Goal progress lookup failed: ID {} not found for user ID: {}", id, currentUser.getId());
                    return new ResourceNotFoundException("Savings goal not found or unauthorized");
                });

        return calculateGoalProgress(goal);
    }

    @Override
    @Transactional
    public SavingsGoalDto.GoalResponse updateGoal(Long id, SavingsGoalDto.UpdateGoalRequest request) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Updating savings goal ID: {} for user ID: {}", id, currentUser.getId());

        // Validate goal ownership
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Goal update failed: ID {} not found for user ID: {}", id, currentUser.getId());
                    return new ResourceNotFoundException("Savings goal not found or unauthorized");
                });

        // Validate updated goal timeline
        if (request.getTargetDate().isBefore(request.getStartDate())) {
            log.warn("Goal update failed: Target date {} is before start date {}", request.getTargetDate(), request.getStartDate());
            throw new BadRequestException("Target date cannot be before the start date");
        }

        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        log.info("Savings goal ID: {} updated successfully", updatedGoal.getId());

        return savingsGoalMapper.toResponse(updatedGoal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Deleting savings goal ID: {} for user ID: {}", id, currentUser.getId());

        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Goal deletion failed: ID {} not found for user ID: {}", id, currentUser.getId());
                    return new ResourceNotFoundException("Savings goal not found or unauthorized");
                });

        savingsGoalRepository.delete(goal);
        log.info("Savings goal ID: {} deleted successfully", id);
    }

    // Dynamic Helper: Calculate income minus expenses since goal's start date
    private SavingsGoalDto.GoalProgressResponse calculateGoalProgress(SavingsGoal goal) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(
                goal.getUser().getId(), CategoryType.INCOME, goal.getStartDate());

        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndCategoryTypeSinceDate(
                goal.getUser().getId(), CategoryType.EXPENSE, goal.getStartDate());

        // Dynamic formula: total income - total expenses since goal start date
        BigDecimal currentProgress = totalIncome.subtract(totalExpense);

        // Progress percentage calculation: (currentProgress / targetAmount) * 100
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                    .multiply(new BigDecimal(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        }

        // Remaining amount calculation: targetAmount - currentProgress
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);

        return savingsGoalMapper.toProgressResponse(goal, currentProgress, progressPercentage, remainingAmount);
    }
}
