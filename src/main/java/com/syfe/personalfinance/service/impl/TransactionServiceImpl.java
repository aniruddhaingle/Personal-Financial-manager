package com.syfe.personalfinance.service.impl;

import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.entity.Category;
import com.syfe.personalfinance.entity.Transaction;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.exception.BadRequestException;
import com.syfe.personalfinance.exception.ResourceNotFoundException;
import com.syfe.personalfinance.mapper.TransactionMapper;
import com.syfe.personalfinance.repository.CategoryRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import com.syfe.personalfinance.service.TransactionService;
import com.syfe.personalfinance.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final UserService userService;

    // Constructor injection only
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  CategoryRepository categoryRepository,
                                  TransactionMapper transactionMapper,
                                  UserService userService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public TransactionDto.TransactionResponse createTransaction(TransactionDto.CreateTransactionRequest request) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Creating new transaction of amount {} for user ID: {}", request.getAmount(), currentUser.getId());

        // Enforce transaction date in the past or present (no future dates allowed)
        if (request.getDate().isAfter(LocalDate.now())) {
            log.warn("Transaction creation failed: Future date {} provided", request.getDate());
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        // Retrieve category, validating that it's either custom owned by the user or a global default
        Category category = categoryRepository.findByIdAndUserAvailable(request.getCategoryId(), currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Transaction creation failed: Category ID {} not found or unauthorized", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found or unavailable for this user");
                });

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setCategory(category);
        transaction.setUser(currentUser);

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto.TransactionResponse> getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            String categoryName,
            CategoryType categoryType,
            Pageable pageable) {

        User currentUser = userService.getAuthenticatedUserEntity();
        log.debug("Fetching transactions for user ID: {} with optional filters: startDate={}, endDate={}, category={}, type={}",
                currentUser.getId(), startDate, endDate, categoryName, categoryType);

        Page<Transaction> transactionPage = transactionRepository.findAllFiltered(
                currentUser.getId(), startDate, endDate, categoryName, categoryType, pageable);

        return transactionPage.map(transactionMapper::toResponse);
    }

    @Override
    @Transactional
    public TransactionDto.TransactionResponse updateTransaction(Long id, TransactionDto.UpdateTransactionRequest request) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Updating transaction ID: {} for user ID: {}", id, currentUser.getId());

        // Validate transaction ownership
        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Transaction update failed: ID {} not found for user ID: {}", id, currentUser.getId());
                    return new ResourceNotFoundException("Transaction not found or unauthorized");
                });

        // Retrieve and validate new category
        Category category = categoryRepository.findByIdAndUserAvailable(request.getCategoryId(), currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Transaction update failed: Category ID {} not found or unauthorized", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found or unavailable for this user");
                });

        // Map updated fields (amount, description, category). Date is immutable and updatable=false, so we keep the original date intact.
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);

        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction ID: {} updated successfully", updatedTransaction.getId());

        return transactionMapper.toResponse(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Deleting transaction ID: {} for user ID: {}", id, currentUser.getId());

        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Transaction deletion failed: ID {} not found for user ID: {}", id, currentUser.getId());
                    return new ResourceNotFoundException("Transaction not found or unauthorized");
                });

        transactionRepository.delete(transaction);
        log.info("Transaction ID: {} deleted successfully", id);
    }
}
