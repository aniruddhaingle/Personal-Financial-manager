package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.TransactionDto
import com.syfe.personalfinance.enums.CategoryType
import java.time.LocalDate

/**
 * Service managing user income and expense transactions.
 */
interface TransactionService {
    /**
     * Creates a new financial transaction for the authenticated user.
     *
     * @param request transaction data including amount, date, category name, and optional description
     * @return the created transaction details including its resolved type
     * @throws com.syfe.personalfinance.exception.BadRequestException if future date or category is invalid
     */
    fun createTransaction(request: TransactionDto.CreateTransactionRequest): TransactionDto.TransactionResponse

    /**
     * Retrieves transactions belonging to the authenticated user, optionally filtered.
     *
     * @param startDate optional filter start date
     * @param endDate optional filter end date
     * @param categoryName optional filter by category name
     * @param categoryId optional filter by category ID
     * @param categoryType optional filter by INCOME/EXPENSE
     * @return list of matching transactions sorted newest first
     */
    fun getTransactions(
        startDate: LocalDate?,
        endDate: LocalDate?,
        categoryName: String?,
        categoryId: Long?,
        categoryType: CategoryType?
    ): List<TransactionDto.TransactionResponse>

    /**
     * Updates mutable fields of an existing transaction (amount, category, description).
     *
     * @param id transaction ID
     * @param request updated transaction fields
     * @return updated transaction details
     * @throws com.syfe.personalfinance.exception.ResourceNotFoundException if transaction not found or unowned
     */
    fun updateTransaction(id: Long, request: TransactionDto.UpdateTransactionRequest): TransactionDto.TransactionResponse

    /**
     * Deletes a transaction by ID for the authenticated user.
     *
     * @param id transaction ID
     * @throws com.syfe.personalfinance.exception.ResourceNotFoundException if transaction not found or unowned
     */
    fun deleteTransaction(id: Long)
}

