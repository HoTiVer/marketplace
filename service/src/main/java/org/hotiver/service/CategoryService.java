package org.hotiver.service;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.repo.CategoryRepo;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<CategoryDto> getCategories() {
        return categoryRepo.findAllSortedByName();
    }

    public void addCategory(CategoryDto categoryDto) {
        if (categoryRepo.existsByName(categoryDto.getName())) {
            throw new EntityAlreadyExistsException("Category with name "
                    + categoryDto.getName() + " already exists");
        }

        Category category = Category.builder()
                    .name(categoryDto.getName())
                    .build();

        categoryRepo.save(category);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with id " + id + " not found");
        }
        categoryRepo.deleteById(id);

    }

    public void editCategory(Long id, CategoryDto categoryDto) {
        var category = categoryRepo.findById(id);

        if (category.isEmpty()){
            throw new EntityNotFoundException("Category with id " + id + " not found");
        }

        var editedCategory = category.get();
        editedCategory.setName(categoryDto.getName());
        categoryRepo.save(editedCategory);
    }
}
