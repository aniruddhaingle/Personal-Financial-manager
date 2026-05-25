package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {
    TransactionDto.TransactionResponse createTransaction(TransactionDto.CreateTransactionRequest request);
    Page<TransactionDto.TransactionResponse> getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            String categoryName,
            CategoryType categoryType,
            Pageable pageable
    );
    TransactionDto.TransactionResponse updateTransaction(Long id, TransactionDto.UpdateTransactionRequest request);
    void deleteTransaction(Long id);
}
