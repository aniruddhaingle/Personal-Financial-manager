package com.syfe.personalfinance.entity;

import com.syfe.personalfinance.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_category_user_name", columnList = "user_id, name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable for global default categories

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
