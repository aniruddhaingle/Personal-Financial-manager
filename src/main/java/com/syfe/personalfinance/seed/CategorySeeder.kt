package com.syfe.personalfinance.seed

import com.syfe.personalfinance.entity.Category
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.repository.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CategorySeeder(
    private val categoryRepository: CategoryRepository
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(CategorySeeder::class.java)

    override fun run(vararg args: String?) {
        log.info("Bootstrapping default global categories...")

        val defaultCategories = listOf(
            Category(name = "Salary", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Food", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Rent", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Transportation", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Entertainment", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Healthcare", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Utilities", type = CategoryType.EXPENSE, isDefault = true)
        )

        for (defaultCat in defaultCategories) {
            if (!categoryRepository.existsByNameIgnoreCaseAndUserIsNull(defaultCat.name)) {
                categoryRepository.save(defaultCat)
                log.debug("Seeded default category: {}", defaultCat.name)
            }
        }

        log.info("Default global categories bootstrap completed successfully.")
    }
}
