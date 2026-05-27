package org.hotiver.app.service.category;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.repo.core.CategoryRepo;
import org.hotiver.repo.projection.CategoryProjectionRepo;
import org.hotiver.service.category.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    @Mock
    private CategoryProjectionRepo categoryProjectionRepo;

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

    @Nested
    class GetCategories {
        @Test
        public void shouldReturnCategories() {
            when(categoryProjectionRepo.findAllSortedByName()).thenReturn(categories);

            List<CategoryDto> result = categoryService.getCategories();

            assertEquals(categories.size(), result.size());
            assertEquals(categories.getFirst().getName(), result.getFirst().getName());

            verify(categoryProjectionRepo, times(1)).findAllSortedByName();
        }

        @Test
        public void shouldReturnCategoriesEmptyList() {
            when(categoryProjectionRepo.findAllSortedByName()).thenReturn(Collections.emptyList());

            List<CategoryDto> result = categoryService.getCategories();

            assertEquals(0, result.size());

            verify(categoryProjectionRepo, times(1)).findAllSortedByName();
        }
    }

    @Nested
    class AddCategory {
        @Test
        public void shouldAddCategorySuccess() {
            CategoryDto categoryDto = new CategoryDto(1L, "Category 1");

            when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(false);

            categoryService.addCategory(categoryDto);

            verify(categoryRepo, times(1)).existsByName(anyString());
            verify(categoryRepo, times(1)).save(any());
        }

        @Test
        public void shouldThrowException_whenCategoryAlreadyExists() {
            CategoryDto categoryDto = new CategoryDto(1L, "Category 1");

            when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class,
                    () -> categoryService.addCategory(categoryDto));

            verify(categoryRepo, times(1)).existsByName(anyString());
            verify(categoryRepo, never()).save(any());
        }
    }

    @Nested
    class DeleteCategory {
        @Test
        public void shouldDeleteCategory() {
            doNothing().when(categoryRepo).deleteById(anyLong());

            categoryService.deleteCategory(anyLong());

            verify(categoryRepo, times(1)).deleteById(anyLong());
        }
    }

    @Nested
    class EditCategory {
        @Test
        public void shouldEditCategory() {
            when(categoryRepo.findById(anyLong()))
                    .thenReturn(Optional.of(new Category(1L, "Category 1")));

            when(categoryRepo.save(any())).thenReturn(new Category(1L, "Category 1"));

            categoryService.editCategory(1L, categories.getFirst());

            verify(categoryRepo, times(1)).save(any());
        }

        @Test
        public void shouldThrowException_whenCategoryNotFound() {
            when(categoryRepo.findById(anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> categoryService.editCategory(1L, categories.getFirst()));

            verify(categoryRepo, never()).save(any());
        }
    }
}
