package com.syfe.personalfinance.controller

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.dto.TransactionDto
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.service.TransactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * REST controller managing creation, retrieval, filtering, mutation, and removal of transactions.
 */
@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    /**
     * Creates a new financial transaction entry with validated amount, past/present date, and category.
     */
    @PostMapping
    fun createTransaction(
        @Valid @RequestBody request: TransactionDto.CreateTransactionRequest
    ): ResponseEntity<TransactionDto.TransactionResponse> {
        val responseData = transactionService.createTransaction(request)
        return ResponseEntity(responseData, HttpStatus.CREATED)
    }

    /**
     * Retrieves transactions belonging to the authenticated user with multi-criteria filtering support.
     */
    @GetMapping
    fun getTransactions(
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) categoryType: CategoryType?
    ): ResponseEntity<TransactionDto.TransactionListResponse> {
        val responseData = transactionService.getTransactions(startDate, endDate, category, categoryId, categoryType)
        val response = TransactionDto.TransactionListResponse(
            transactions = responseData
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Updates editable fields of a transaction (amount, category, description), leaving date immutable.
     */
    @PutMapping("/{id}")
    fun updateTransaction(
        @PathVariable id: Long,
        @Valid @RequestBody request: TransactionDto.UpdateTransactionRequest
    ): ResponseEntity<TransactionDto.TransactionResponse> {
        val responseData = transactionService.updateTransaction(id, request)
        return ResponseEntity.ok(responseData)
    }

    /**
     * Deletes a transaction and removes its effect from savings calculations and analytics reports.
     */
    @DeleteMapping("/{id}")
    fun deleteTransaction(@PathVariable id: Long): ResponseEntity<AuthDto.SimpleMessageResponse> {
        transactionService.deleteTransaction(id)
        val response = AuthDto.SimpleMessageResponse(
            message = "Transaction deleted successfully"
        )
        return ResponseEntity.ok(response)
    }
}
