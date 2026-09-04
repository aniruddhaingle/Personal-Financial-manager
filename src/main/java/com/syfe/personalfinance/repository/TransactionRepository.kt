package com.syfe.personalfinance.repository

import com.syfe.personalfinance.entity.Transaction
import com.syfe.personalfinance.enums.CategoryType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): Optional<Transaction>

    @Query(
        "SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.user.id = :userId " +
        "AND (:startDate IS NULL OR t.date >= :startDate) " +
        "AND (:endDate IS NULL OR t.date <= :endDate) " +
        "AND (:categoryName IS NULL OR LOWER(t.category.name) = LOWER(:categoryName)) " +
        "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
        "AND (:categoryType IS NULL OR t.category.type = :categoryType) " +
        "ORDER BY t.date DESC, t.id DESC"
    )
    fun findAllFiltered(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate?,
        @Param("endDate") endDate: LocalDate?,
        @Param("categoryName") categoryName: String?,
        @Param("categoryId") categoryId: Long?,
        @Param("categoryType") categoryType: CategoryType?
    ): List<Transaction>

    fun existsByCategoryId(categoryId: Long): Boolean

    @Query(
        "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.user.id = :userId " +
        "AND t.category.type = :type " +
        "AND t.date >= :startDate"
    )
    fun sumAmountByUserIdAndCategoryTypeSinceDate(
        @Param("userId") userId: Long,
        @Param("type") type: CategoryType,
        @Param("startDate") startDate: LocalDate
    ): BigDecimal?

    @Query(
        "SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.user.id = :userId " +
        "AND t.category.type = :type " +
        "AND t.date >= :startDate AND t.date <= :endDate " +
        "GROUP BY t.category.name"
    )
    fun sumByCategoryAndDateRange(
        @Param("userId") userId: Long,
        @Param("type") type: CategoryType,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Array<Any>>
}
