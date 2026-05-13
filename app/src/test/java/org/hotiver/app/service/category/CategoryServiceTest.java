package org.hotiver.app.service.category;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.service.category.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepo categoryRepo;

    @InjectMocks
    private CategoryService categoryService;

    private List<CategoryDto> categories;

    @BeforeEach
    public void setup() {
        categories = List.of(
                new CategoryDto(1L, "Category 1"),
                new CategoryDto(2L, "Category 2")
        );
    }

    @Test
    public void get_all_categories() {
        when(categoryRepo.findAllSortedByName()).thenReturn(categories);

        List<CategoryDto> result = categoryService.getCategories();

        assertEquals(categories.size(), result.size());
        assertEquals(categories.getFirst().getName(), result.getFirst().getName());

        verify(categoryRepo, times(1)).findAllSortedByName();
    }

    @Test
    public void get_all_categories_empty() {
        when(categoryRepo.findAllSortedByName()).thenReturn(Collections.emptyList());

        List<CategoryDto> result = categoryService.getCategories();

        assertEquals(0, result.size());

        verify(categoryRepo, times(1)).findAllSortedByName();
    }

    @Test
    public void add_category_success() {
        CategoryDto categoryDto = new CategoryDto(1L, "Category 1");

        when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(false);

        categoryService.addCategory(categoryDto);

        verify(categoryRepo, times(1)).existsByName(anyString());
        verify(categoryRepo, times(1)).save(any());
    }

    @Test
    public void add_category_already_exists() {
        CategoryDto categoryDto = new CategoryDto(1L, "Category 1");

        when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> categoryService.addCategory(categoryDto));

        verify(categoryRepo, times(1)).existsByName(anyString());
        verify(categoryRepo, never()).save(any());
    }

    @Test
    public void delete_category_success() {
        doNothing().when(categoryRepo).deleteById(anyLong());

        categoryService.deleteCategory(anyLong());

        verify(categoryRepo, times(1)).deleteById(anyLong());
    }

    @Test
    public void edit_category_success() {
        when(categoryRepo.findById(anyLong()))
                .thenReturn(Optional.of(new Category(1L, "Category 1")));

        when(categoryRepo.save(any())).thenReturn(new Category(1L, "Category 1"));

        categoryService.editCategory(1L, categories.getFirst());

        verify(categoryRepo, times(1)).save(any());
    }

    @Test
    public void edit_category_not_found() {
        when(categoryRepo.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryService.editCategory(1L, categories.getFirst()));

        verify(categoryRepo, never()).save(any());
    }
}
