package org.hotiver.repo;


import org.hotiver.domain.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM category c ORDER BY c.name ASC")
    List<Category> findAllSortedByName();

    Optional<Category> findByName(String categoryName);
}
