package org.hotiver.repo;


import jakarta.validation.constraints.NotEmpty;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.category.CategoryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    @Query(value = """
        SELECT
                c.id as id,
                c.name as name
        FROM category c
        WHERE c.name != 'empty'
        ORDER BY c.name""", nativeQuery = true)
    List<CategoryDto> findAllSortedByName();

    Optional<Category> findByName(String categoryName);

    @Query(value = """
        SELECT
                c.id as id,
                c.name as name
        FROM category c
        WHERE c.name != 'empty'
        """, nativeQuery = true)
    List<CategoryDto> findCategoryAndConvertToDto();

    boolean existsByName(@NotEmpty(message = "category name cannot be empty") String name);
}
