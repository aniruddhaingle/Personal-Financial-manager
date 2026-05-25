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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final UserService userService;

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

        if (request.getDate().isAfter(LocalDate.now())) {
            log.warn("Transaction creation failed: Future date {} provided", request.getDate());
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        Category category = categoryRepository.findByNameIgnoreCaseAndUserAvailable(request.getCategory(), currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Transaction creation failed: Category '{}' not found or unauthorized", request.getCategory());
                    return new BadRequestException("Category not found or unavailable for this user");
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
    public List<TransactionDto.TransactionResponse> getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            String categoryName,
            Long categoryId,
            CategoryType categoryType) {

        User currentUser = userService.getAuthenticatedUserEntity();
        log.debug("Fetching transactions for user ID: {} with optional filters: startDate={}, endDate={}, categoryName={}, categoryId={}, type={}",
                currentUser.getId(), startDate, endDate, categoryName, categoryId, categoryType);

        List<Transaction> transactions = transactionRepository.findAllFiltered(
                currentUser.getId(), startDate, endDate, categoryName, categoryId, categoryType);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDto.TransactionResponse updateTransaction(Long id, TransactionDto.UpdateTransactionRequest request) {
        User currentUser = userService.getAuthenticatedUserEntity();
        log.info("Updating transaction ID: {} for user ID: {}", id, currentUser.getId());

        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Transaction update failed: ID {} not found for user ID: {}", id, currentUser.getId());
                    return new ResourceNotFoundException("Transaction not found or unauthorized");
                });

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findByNameIgnoreCaseAndUserAvailable(request.getCategory(), currentUser.getId())
                    .orElseThrow(() -> {
                        log.warn("Transaction update failed: Category '{}' not found or unauthorized", request.getCategory());
                        return new BadRequestException("Category not found or unavailable for this user");
                    });
            transaction.setCategory(category);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

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
