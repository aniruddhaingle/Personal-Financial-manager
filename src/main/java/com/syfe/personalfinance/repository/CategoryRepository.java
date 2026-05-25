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

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.isDefault DESC, c.name ASC")
    List<Category> findAllAvailableToUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByIdAndUserAvailable(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByNameIgnoreCaseAndUserAvailable(@Param("name") String name, @Param("userId") Long userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    boolean existsByNameIgnoreCaseAndUserIsNull(String name);
}
