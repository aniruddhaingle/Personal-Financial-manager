package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.enums.CategoryType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    TransactionDto.TransactionResponse createTransaction(TransactionDto.CreateTransactionRequest request);
    List<TransactionDto.TransactionResponse> getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            String categoryName,
            Long categoryId,
            CategoryType categoryType
    );
    TransactionDto.TransactionResponse updateTransaction(Long id, TransactionDto.UpdateTransactionRequest request);
    void deleteTransaction(Long id);
}
