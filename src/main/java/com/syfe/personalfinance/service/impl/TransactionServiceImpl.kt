package com.syfe.personalfinance.service.impl

import com.syfe.personalfinance.dto.TransactionDto
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.exception.BadRequestException
import com.syfe.personalfinance.exception.ResourceNotFoundException
import com.syfe.personalfinance.mapper.TransactionMapper
import com.syfe.personalfinance.repository.CategoryRepository
import com.syfe.personalfinance.repository.TransactionRepository
import com.syfe.personalfinance.service.TransactionService
import com.syfe.personalfinance.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionMapper: TransactionMapper,
    private val userService: UserService
) : TransactionService {

    private val log = LoggerFactory.getLogger(TransactionServiceImpl::class.java)

    @Transactional
    override fun createTransaction(request: TransactionDto.CreateTransactionRequest): TransactionDto.TransactionResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Creating new transaction of amount {} for user ID: {}", request.amount, currentUser.id)

        val date = request.date ?: LocalDate.now()
        if (date.isAfter(LocalDate.now())) {
            log.warn("Transaction creation failed: Future date {} provided", date)
            throw BadRequestException("Transaction date cannot be in the future")
        }

        val category = categoryRepository.findByNameIgnoreCaseAndUserAvailable(request.category, currentUser.id!!)
            .orElseThrow {
                log.warn("Transaction creation failed: Category '{}' not found or unauthorized", request.category)
                BadRequestException("Category not found or unavailable for this user")
            }

        val transaction = transactionMapper.toEntity(request) ?: throw IllegalStateException("Failed to map transaction entity")
        transaction.category = category
        transaction.user = currentUser

        val savedTransaction = transactionRepository.save(transaction)
        log.info("Transaction created successfully with ID: {}", savedTransaction.id)

        return transactionMapper.toResponse(savedTransaction) ?: throw IllegalStateException("Failed to map transaction response")
    }

    @Transactional(readOnly = true)
    override fun getTransactions(
        startDate: LocalDate?,
        endDate: LocalDate?,
        categoryName: String?,
        categoryId: Long?,
        categoryType: CategoryType?
    ): List<TransactionDto.TransactionResponse> {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.debug(
            "Fetching transactions for user ID: {} with optional filters: startDate={}, endDate={}, categoryName={}, categoryId={}, type={}",
            currentUser.id, startDate, endDate, categoryName, categoryId, categoryType
        )

        val transactions = transactionRepository.findAllFiltered(
            currentUser.id!!, startDate, endDate, categoryName, categoryId, categoryType
        )

        return transactions.mapNotNull { transactionMapper.toResponse(it) }
    }

    @Transactional
    override fun updateTransaction(id: Long, request: TransactionDto.UpdateTransactionRequest): TransactionDto.TransactionResponse {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Updating transaction ID: {} for user ID: {}", id, currentUser.id)

        val transaction = transactionRepository.findByIdAndUserId(id, currentUser.id!!)
            .orElseThrow {
                log.warn("Transaction update failed: ID {} not found for user ID: {}", id, currentUser.id)
                ResourceNotFoundException("Transaction not found or unauthorized")
            }

        if (request.amount != null) {
            transaction.amount = request.amount
        }

        if (request.category != null) {
            val category = categoryRepository.findByNameIgnoreCaseAndUserAvailable(request.category, currentUser.id!!)
                .orElseThrow {
                    log.warn("Transaction update failed: Category '{}' not found or unauthorized", request.category)
                    BadRequestException("Category not found or unavailable for this user")
                }
            transaction.category = category
        }

        if (request.description != null) {
            transaction.description = request.description
        }

        val updatedTransaction = transactionRepository.save(transaction)
        log.info("Transaction ID: {} updated successfully", updatedTransaction.id)

        return transactionMapper.toResponse(updatedTransaction) ?: throw IllegalStateException("Failed to map transaction response")
    }

    @Transactional
    override fun deleteTransaction(id: Long) {
        val currentUser = userService.getAuthenticatedUserEntity()
        log.info("Deleting transaction ID: {} for user ID: {}", id, currentUser.id)

        val transaction = transactionRepository.findByIdAndUserId(id, currentUser.id!!)
            .orElseThrow {
                log.warn("Transaction deletion failed: ID {} not found for user ID: {}", id, currentUser.id)
                ResourceNotFoundException("Transaction not found or unauthorized")
            }

        transactionRepository.delete(transaction)
        log.info("Transaction ID: {} deleted successfully", id)
    }
}
