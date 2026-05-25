package com.syfe.personalfinance.mapper;

import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDto.TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionDto.TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .description(transaction.getDescription())
                .category(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .type(transaction.getCategory() != null ? transaction.getCategory().getType() : null)
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
