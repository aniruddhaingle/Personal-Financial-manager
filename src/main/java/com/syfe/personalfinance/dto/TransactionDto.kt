package com.syfe.personalfinance.dto

import com.syfe.personalfinance.enums.CategoryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

class TransactionDto {

    data class CreateTransactionRequest(
        @field:NotNull(message = "Transaction amount is required")
        @field:Positive(message = "Amount must be a positive number")
        val amount: BigDecimal? = null,

        @field:NotNull(message = "Transaction date is required")
        @field:PastOrPresent(message = "Transaction date cannot be in the future")
        val date: LocalDate? = null,

        @field:NotBlank(message = "Category name is required")
        val category: String = "",

        val description: String? = null
    )

    data class UpdateTransactionRequest(
        @field:Positive(message = "Amount must be a positive number")
        val amount: BigDecimal? = null,

        val category: String? = null,

        val description: String? = null
    )

    data class TransactionResponse(
        val id: Long? = null,
        val amount: BigDecimal? = null,
        val date: LocalDate? = null,
        val category: String? = null,
        val description: String? = null,
        val type: CategoryType? = null
    )

    data class TransactionListResponse(
        val transactions: List<TransactionResponse> = emptyList()
    )
}
