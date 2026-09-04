package com.syfe.personalfinance.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "savings_goals",
    indexes = [
        Index(name = "idx_goal_user_dates", columnList = "user_id, start_date, target_date")
    ]
)
class SavingsGoal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "goal_name", nullable = false)
    var goalName: String = "",

    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    var targetAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(name = "target_date", nullable = false)
    var targetDate: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SavingsGoal) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "SavingsGoal(id=$id, goalName='$goalName', targetAmount=$targetAmount, targetDate=$targetDate)"
}
