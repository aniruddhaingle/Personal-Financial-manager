package com.syfe.personalfinance.repository

import com.syfe.personalfinance.entity.SavingsGoal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SavingsGoalRepository : JpaRepository<SavingsGoal, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): Optional<SavingsGoal>

    fun findByUserId(userId: Long): List<SavingsGoal>
}
