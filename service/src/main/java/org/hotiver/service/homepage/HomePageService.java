package org.hotiver.service.homepage;

import lombok.RequiredArgsConstructor;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.dto.home.HomePageDto;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.projection.CategoryProjectionRepo;
import org.hotiver.repo.projection.ProductProjectionRepo;
import org.hotiver.service.product.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomePageService {

    private final CategoryProjectionRepo categoryProjectionRepo;
    private final ProductImageService productImageService;
    private final ProductProjectionRepo productProjectionRepo;
    private final Integer limitForFeaturesProducts = 8;
    private final Integer limitForNewProducts = 8;
    private final Integer limitForPopularProducts = 8;

    public HomePageDto getMainPage() {
        List<CategoryDto> categories = categoryProjectionRepo.findCategoryAndConvertToDto();

        List<ListProductDto> featured = productProjectionRepo
                .findRandomVisibleProducts(limitForFeaturesProducts);
        productImageService.addHostToImage(featured);

        List<ListProductDto> newProducts = productProjectionRepo
                .findNewestVisibleProducts(limitForNewProducts);
        productImageService.addHostToImage(newProducts);

        List<ListProductDto> popularProducts = productProjectionRepo
                .findPopularVisibleProducts(limitForPopularProducts);
        productImageService.addHostToImage(popularProducts);

        return new HomePageDto(categories, featured, newProducts, popularProducts);
    }
}
