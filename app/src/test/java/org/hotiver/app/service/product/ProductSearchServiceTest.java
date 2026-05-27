package org.hotiver.app.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.core.CategoryRepo;
import org.hotiver.repo.query.ProductQueryRepo;
import org.hotiver.service.product.ProductImageService;
import org.hotiver.service.product.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductSearchServiceTest {

    @Mock
    private ProductQueryRepo productQueryRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Page<ListProductDto> page;
    private Page<ListProductDto> emptyPage;
    private Category category;

    private final int pageNumber = 0;
    private final int size = 10;
    private final String keyword = "test";

    @BeforeEach
    void setUp() {
        List<ListProductDto> products = List.of(
                new ListProductDto(1L, "test product 1", BigDecimal.valueOf(99.99), "image1.jpg"),
                new ListProductDto(2L, "test product 2", BigDecimal.valueOf(149.99), "image2.jpg")
        );

        Pageable pageable = PageRequest.of(pageNumber, size);

        page = new PageImpl<>(products, pageable, products.size());
        emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        category = new Category();
        category.setId(1L);
        category.setName(keyword);
    }

    @Nested
    class ProductSearchByKeyWords {
        @Test
        public void shouldReturnNotEmptyPage() {
            when(productQueryRepo.findByKeyWord("test", PageRequest.of(pageNumber, size)))
                    .thenReturn(page);

            productSearchService.productSearchByKeyWords("test", pageNumber, size);

            verify(productQueryRepo).findByKeyWord("test", PageRequest.of(pageNumber, size));
            verify(productImageService).addHostToImage(page);
        }

        @Test
        public void shouldReturnEmptyPage() {
            when(productQueryRepo.findByKeyWord("test", PageRequest.of(pageNumber, size)))
                    .thenReturn(page);

            productSearchService.productSearchByKeyWords("test", pageNumber, size);

            verify(productQueryRepo).findByKeyWord("test", PageRequest.of(pageNumber, size));
            verify(productImageService).addHostToImage(page);
        }
    }

    @Nested
    class ProductSearchByCategory {
        @Test
        public void shouldReturnNotEmptyPage() {
            when(productQueryRepo.findByCategory("test", PageRequest.of(pageNumber, size)))
                    .thenReturn(page);

            when(categoryRepo.findByName("test"))
                    .thenReturn(Optional.of(category));

            productSearchService.productSearchByCategory("test", pageNumber, size);

            verify(productQueryRepo).findByCategory("test", PageRequest.of(pageNumber, size));
            verify(productImageService).addHostToImage(page);
        }

        @Test
        public void shouldReturnEmptyPage() {
            when(productQueryRepo.findByCategory("test", PageRequest.of(pageNumber, size)))
                    .thenReturn(page);

            when(categoryRepo.findByName("test"))
                    .thenReturn(Optional.of(category));

            productSearchService.productSearchByCategory("test", pageNumber, size);

            verify(productQueryRepo).findByCategory("test", PageRequest.of(pageNumber, size));
            verify(productImageService).addHostToImage(page);
        }

        @Test
        public void shouldThrowException_whenCategoryNotFound() {
            when(categoryRepo.findByName("test"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                productSearchService
                        .productSearchByCategory("test", pageNumber, size);
            });

            verify(productQueryRepo, never()).findByCategory("test", PageRequest.of(pageNumber, size));
            verify(productImageService, never()).addHostToImage(page);
        }
    }
}
