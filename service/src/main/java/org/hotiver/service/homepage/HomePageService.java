package org.hotiver.service.homepage;

import org.hotiver.dto.category.CategoryDto;
import org.hotiver.dto.home.HomePageDto;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomePageService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final Integer limitForFeaturesProducts = 8;
    private final Integer limitForNewProducts = 8;
    private final Integer limitForPopularProducts = 8;
    @Value("${storage.host}")
    private String imageStorageHost;

    public HomePageService(ProductRepo productRepo, CategoryRepo categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    public HomePageDto getMainPage() {
        List<CategoryDto> categories = categoryRepo.findCategoryAndConvertToDto();

        List<ListProductDto> featured = productRepo
                .findRandomVisibleProducts(limitForFeaturesProducts);
        addHostToImage(featured);

        List<ListProductDto> newProducts = productRepo
                .findNewestVisibleProducts(limitForNewProducts);
        addHostToImage(newProducts);

        List<ListProductDto> popularProducts = productRepo
                .findPopularVisibleProducts(limitForPopularProducts);
        addHostToImage(popularProducts);

        return new HomePageDto(categories, featured, newProducts, popularProducts);
    }

    private void addHostToImage(List<ListProductDto> productImages) {
        var images = productImages;
        for (ListProductDto productImage : productImages) {
            productImage.setMainImageUrl(imageStorageHost + "/images"
                    + productImage.getMainImageUrl());
        }

    }
}
