package org.hotiver.repo.core;


import jakarta.validation.constraints.NotEmpty;
import org.hotiver.domain.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String categoryName);

    boolean existsByName(@NotEmpty(message = "category name cannot be empty") String name);
}
