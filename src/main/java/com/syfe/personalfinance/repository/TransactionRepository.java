package com.syfe.personalfinance.repository;

import com.syfe.personalfinance.entity.Transaction;
import com.syfe.personalfinance.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.user.id = :userId " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:categoryName IS NULL OR LOWER(t.category.name) = LOWER(:categoryName)) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:categoryType IS NULL OR t.category.type = :categoryType) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findAllFiltered(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryName") String categoryName,
            @Param("categoryId") Long categoryId,
            @Param("categoryType") CategoryType categoryType
    );

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.type = :type " +
           "AND t.date >= :startDate")
    BigDecimal sumAmountByUserIdAndCategoryTypeSinceDate(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate
    );

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.type = :type " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "GROUP BY t.category.name")
    List<Object[]> sumByCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
