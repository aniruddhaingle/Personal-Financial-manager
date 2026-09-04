package com.syfe.personalfinance.mapper

import com.syfe.personalfinance.dto.TransactionDto
import com.syfe.personalfinance.entity.Transaction
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class TransactionMapper {

    fun toResponse(transaction: Transaction?): TransactionDto.TransactionResponse? {
        if (transaction == null) {
            return null
        }
        return TransactionDto.TransactionResponse(
            id = transaction.id,
            amount = transaction.amount,
            date = transaction.date,
            category = transaction.category?.name,
            description = transaction.description,
            type = transaction.category?.type
        )
    }

    fun toEntity(request: TransactionDto.CreateTransactionRequest?): Transaction? {
        if (request == null) {
            return null
        }
        return Transaction(
            amount = request.amount ?: BigDecimal.ZERO,
            date = request.date ?: LocalDate.now(),
            description = request.description
        )
    }
}
