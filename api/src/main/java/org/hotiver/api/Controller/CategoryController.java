package org.hotiver.api.Controller;

import jakarta.validation.Valid;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/category")
    public List<CategoryDto> getCategories(){
        return categoryService.getCategories();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/category")
    public ResponseEntity<?> addCategory(@Valid @RequestBody CategoryDto categoryDto){
        categoryService.addCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/category/{id}")
    public ResponseEntity deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/category/{id}")
    public ResponseEntity<?> editCategory(@PathVariable Long id,
                                          @Valid @RequestBody CategoryDto categoryDto){
        categoryService.editCategory(id, categoryDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
