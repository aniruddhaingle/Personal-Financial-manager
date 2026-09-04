package com.syfe.personalfinance.repository

import com.syfe.personalfinance.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.isDefault DESC, c.name ASC")
    fun findAllAvailableToUser(@Param("userId") userId: Long): List<Category>

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.user IS NULL)")
    fun findByIdAndUserAvailable(@Param("id") id: Long, @Param("userId") userId: Long): Optional<Category>

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND (c.user.id = :userId OR c.user IS NULL)")
    fun findByNameIgnoreCaseAndUserAvailable(@Param("name") name: String, @Param("userId") userId: Long): Optional<Category>

    fun existsByNameIgnoreCaseAndUserId(name: String, userId: Long): Boolean

    fun existsByNameIgnoreCaseAndUserIsNull(name: String): Boolean
}
