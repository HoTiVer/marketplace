package org.hotiver.service;

import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.repo.CategoryRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<CategoryDto> getCategories() {
        List<Category> categories = categoryRepo.findAll();
        List<CategoryDto> returnCategory = new ArrayList<>();

        for (var category : categories) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName(category.getName());
            categoryDto.setId(category.getId());
            returnCategory.add(categoryDto);

        }
        return returnCategory;
    }

    public ResponseEntity<?> addCategory(CategoryDto categoryDto) {
        String categoryName = categoryDto.getName();

        Category category = new Category();
        category.setName(categoryName);

        try {
            categoryRepo.save(category);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public void deleteCategory(Long id) {
        categoryRepo.deleteById(id);
    }

    public ResponseEntity<?> editCategory(Long id, CategoryDto categoryDto) {
        var category = categoryRepo.findById(id);

        if (category.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        var editedCategory = category.get();
        editedCategory.setName(categoryDto.getName());
        try {
            categoryRepo.save(editedCategory);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
