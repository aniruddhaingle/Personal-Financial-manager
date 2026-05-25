package com.syfe.personalfinance.repository;

import com.syfe.personalfinance.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    // Retrieve savings goal isolated by id and user
    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);

    // Retrieve all savings goals for a specific user
    List<SavingsGoal> findByUserId(Long userId);
}
