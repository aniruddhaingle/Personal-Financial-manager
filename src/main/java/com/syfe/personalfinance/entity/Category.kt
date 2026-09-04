package com.syfe.personalfinance.entity

import com.syfe.personalfinance.enums.CategoryType
import jakarta.persistence.*

@Entity
@Table(
    name = "categories",
    indexes = [
        Index(name = "idx_category_user_name", columnList = "user_id, name")
    ]
)
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: CategoryType = CategoryType.EXPENSE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Category(id=$id, name='$name', type=$type, isDefault=$isDefault)"
}
