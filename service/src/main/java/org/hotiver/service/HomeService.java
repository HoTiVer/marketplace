package org.hotiver.service;

import org.hotiver.dto.category.CategoryDto;
import org.hotiver.dto.home.HomePageDto;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final Integer limitForFeaturesProducts = 8;
    private final Integer limitForNewProducts = 8;
    private final Integer limitForPopularProducts = 8;

    public HomeService(ProductRepo productRepo, CategoryRepo categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    public HomePageDto getMainPage() {
        List<CategoryDto> categories = categoryRepo.findCategoryAndConvertToDto();

        List<ListProductDto> featured = productRepo
                .findRandomVisibleProducts(limitForFeaturesProducts);

        List<ListProductDto> newProducts = productRepo
                .findNewestVisibleProducts(limitForNewProducts);

        List<ListProductDto> popularProducts = productRepo
                .findPopularVisibleProducts(limitForPopularProducts);

        return new HomePageDto(categories, featured, newProducts, popularProducts);
    }
}
