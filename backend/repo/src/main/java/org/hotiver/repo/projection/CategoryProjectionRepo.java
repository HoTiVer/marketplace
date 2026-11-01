package org.hotiver.repo.projection;

import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.category.CategoryDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CategoryProjectionRepo extends Repository<Category, Long> {

    @Query(value = """
        SELECT
                c.id as id,
                c.name as name
        FROM category c
        WHERE c.name != 'empty'
        ORDER BY c.name""", nativeQuery = true)
    List<CategoryDto> findAllSortedByName();

    @Query(value = """
        SELECT
                c.id as id,
                c.name as name
        FROM category c
        WHERE c.name != 'empty'
        """, nativeQuery = true)
    List<CategoryDto> findCategoryAndConvertToDto();
}
