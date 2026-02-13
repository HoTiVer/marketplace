package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String categoryKey = "categories";

    public CategoryService(CategoryRepo categoryRepo, RedisService redisService) {
        this.categoryRepo = categoryRepo;
        this.redisService = redisService;
    }

    public List<CategoryDto> getCategories() {
        return categoryRepo.findAllSortedByName();
    }

    public ResponseEntity<?> addCategory(CategoryDto categoryDto) {
        String categoryName = categoryDto.getName();
        if (categoryName == null) {
            return ResponseEntity.badRequest().build();
        }

        Category category = new Category();
        category.setName(categoryName);

        try {
            categoryRepo.save(category);
            redisService.deleteValue(categoryKey);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public void deleteCategory(Long id) {
        categoryRepo.deleteById(id);
        redisService.deleteValue(categoryKey);
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
            redisService.deleteValue(categoryKey);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
