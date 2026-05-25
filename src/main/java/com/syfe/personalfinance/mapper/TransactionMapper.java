package com.syfe.personalfinance.mapper;

import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    public TransactionMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public TransactionDto.TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionDto.TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .description(transaction.getDescription())
                .category(categoryMapper.toResponse(transaction.getCategory()))
                .build();
    }

    public Transaction toEntity(TransactionDto.CreateTransactionRequest request) {
        if (request == null) {
            return null;
        }
        return Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .build();
    }
}
