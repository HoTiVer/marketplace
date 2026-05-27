package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.core.CategoryRepo;
import org.hotiver.repo.query.ProductQueryRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductQueryRepo productQueryRepo;
    private final CategoryRepo categoryRepo;
    private final ProductImageService productImageService;

    public Page<ListProductDto> productSearchByKeyWords(
            String searchTerm,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ListProductDto> products = productQueryRepo.findByKeyWord(searchTerm, pageable);
        productImageService.addHostToImage(products);
        return products;
    }

    public Page<ListProductDto> productSearchByCategory(
            String searchingCategory,
            int page,
            int size) {
        Long categoryId = parseCategoryId(searchingCategory);

        Optional<Category> opCategory = categoryRepo.findById(categoryId);
        if (opCategory.isEmpty()) {
            opCategory = categoryRepo.findByName(searchingCategory);
            if (opCategory.isEmpty()) {
                throw new EntityNotFoundException("Category not found");
            }
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<ListProductDto> products = productQueryRepo.findByCategory(
                opCategory.get().getName(),
                pageable);

        productImageService.addHostToImage(products);
        return products;
    }

    private Long parseCategoryId(String category) {
        try {
            return Long.parseLong(category);
        }  catch (NumberFormatException e) {
            return -1L;
        }
    }
}
