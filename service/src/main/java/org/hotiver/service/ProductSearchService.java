package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.domain.Entity.Category;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.ProductProjection;
import org.hotiver.dto.product.ProductSearchDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductSearchService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;

    public ProductSearchService(ProductRepo productRepo, CategoryRepo categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    public ResponseEntity<Page<ProductProjection>> productSearchByKeyWords(
            String searchTerm,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok()
                .body(productRepo.findByKeyWord(searchTerm, pageable));
    }

    public ResponseEntity<Page<ProductProjection>> productSearchByCategory(
            String searchingCategory,
            int page,
            int size) {
        Long categoryId = parseCategoryId(searchingCategory);

        Optional<Category> opCategory = categoryRepo.findById(categoryId);
        if (opCategory.isEmpty()) {
            opCategory = categoryRepo.findByName(searchingCategory);
            if (opCategory.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductProjection> products = productRepo.findByCategory(opCategory.get().getName(), pageable);

        return ResponseEntity.ok().body(products);
    }

    private Long parseCategoryId(String category) {
        try {
            return Long.parseLong(category);
        }  catch (NumberFormatException e) {
            return -1L;
        }
    }
}
