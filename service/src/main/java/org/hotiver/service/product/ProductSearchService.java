package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductSearchService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductImageService productImageService;

    public ProductSearchService(ProductRepo productRepo, CategoryRepo categoryRepo,
                                ProductImageService productImageService) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productImageService = productImageService;
    }

    public Page<ListProductDto> productSearchByKeyWords(
            String searchTerm,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ListProductDto> products = productRepo.findByKeyWord(searchTerm, pageable);
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

        Page<ListProductDto> products = productRepo.findByCategory(
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
