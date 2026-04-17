package org.hotiver.service.homepage;

import org.hotiver.dto.category.CategoryDto;
import org.hotiver.dto.home.HomePageDto;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.service.product.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomePageService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductImageService productImageService;
    private final Integer limitForFeaturesProducts = 8;
    private final Integer limitForNewProducts = 8;
    private final Integer limitForPopularProducts = 8;

    public HomePageService(ProductRepo productRepo, CategoryRepo categoryRepo,
                           ProductImageService productImageService) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productImageService = productImageService;
    }

    public HomePageDto getMainPage() {
        List<CategoryDto> categories = categoryRepo.findCategoryAndConvertToDto();

        List<ListProductDto> featured = productRepo
                .findRandomVisibleProducts(limitForFeaturesProducts);
        productImageService.addHostToImage(featured);

        List<ListProductDto> newProducts = productRepo
                .findNewestVisibleProducts(limitForNewProducts);
        productImageService.addHostToImage(newProducts);

        List<ListProductDto> popularProducts = productRepo
                .findPopularVisibleProducts(limitForPopularProducts);
        productImageService.addHostToImage(popularProducts);

        return new HomePageDto(categories, featured, newProducts, popularProducts);
    }
}
