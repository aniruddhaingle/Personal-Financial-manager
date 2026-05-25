package com.syfe.personalfinance.seed;

import com.syfe.personalfinance.entity.Category;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Bootstrapping default global categories...");

        List<Category> defaultCategories = Arrays.asList(
            Category.builder().name("Salary").type(CategoryType.INCOME).isDefault(true).build(),
            Category.builder().name("Food").type(CategoryType.EXPENSE).isDefault(true).build(),
            Category.builder().name("Rent").type(CategoryType.EXPENSE).isDefault(true).build(),
            Category.builder().name("Transportation").type(CategoryType.EXPENSE).isDefault(true).build(),
            Category.builder().name("Entertainment").type(CategoryType.EXPENSE).isDefault(true).build(),
            Category.builder().name("Healthcare").type(CategoryType.EXPENSE).isDefault(true).build(),
            Category.builder().name("Utilities").type(CategoryType.EXPENSE).isDefault(true).build()
        );

        for (Category defaultCat : defaultCategories) {
            if (!categoryRepository.existsByNameIgnoreCaseAndUserIsNull(defaultCat.getName())) {
                categoryRepository.save(defaultCat);
                log.debug("Seeded default category: {}", defaultCat.getName());
            }
        }

        log.info("Default global categories bootstrap completed successfully.");
    }
}
