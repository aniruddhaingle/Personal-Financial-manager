package com.syfe.personalfinance.repository;

import com.syfe.personalfinance.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Fetch all categories readable by the user: default categories + their own custom ones
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.isDefault DESC, c.name ASC")
    List<Category> findAllAvailableToUser(@Param("userId") Long userId);

    // Fetch a specific category checking if it is available to the user
    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByIdAndUserAvailable(@Param("id") Long id, @Param("userId") Long userId);

    // Check if custom category name already exists for user (case-insensitive)
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    // Check if a global default category already exists with the name (case-insensitive)
    boolean existsByNameIgnoreCaseAndUserIsNull(String name);
}
