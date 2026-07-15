package org.hotiver.service.product;

import lombok.AllArgsConstructor;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.ProductPromotion;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ProductPromotionDto;
import org.hotiver.dto.product.ProductPromotionRequest;
import org.hotiver.dto.product.SellerProductPromotionDto;
import org.hotiver.repo.core.ProductPromotionRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.mapper.ProductPromotionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductPromotionService {

    private final ProductPromotionRepo productPromotionRepo;
    private final ProductRepo productRepo;
    private final CurrentUserService currentUserService;
    private final ProductPromotionMapper productPromotionMapper;

    public ProductPromotionDto getProductPromotion(Product product) {
        var productPromotion = productPromotionRepo
                .findActiveProductPromotion(product.getId(), Instant.now()).orElse(null);

        if (productPromotion != null) {
            BigDecimal promotionPrice =
                    calculatePromotionPrice(product, productPromotion.getDiscountPercent());

            OffsetDateTime dateTime = null;
            if (productPromotion.isShowEndDate()){
                dateTime = productPromotion.getEndTime().atOffset(ZoneOffset.UTC);
            }
            return new ProductPromotionDto(
                    true,
                    product.getPrice(),
                    promotionPrice,
                    dateTime
            );
        }
        return new ProductPromotionDto(
            false,
                product.getPrice(),
                product.getPrice(),
                null
        );
    }

    //TODO SHOW PRICE WITH SALE AND WITHOUT SALE
    public List<SellerProductPromotionDto> getProductPromotions(Long productId) {
        Product product = productRepo.findById(productId).orElse(null);
        validateProductOwnership(product);

        List<ProductPromotion> productPromotions = productPromotionRepo
                .findProductPromotions(productId);

        List<SellerProductPromotionDto> result = new ArrayList<>();

        for (ProductPromotion promotion : productPromotions) {
            result.add(productPromotionMapper.entityToSellerProductPromotionDto(promotion));
        }

        return result;
    }

    private BigDecimal calculatePromotionPrice(Product product, Integer percent) {
        return product.getPrice()
                .subtract(product.getPrice()
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100)));
    }

    public ProductPromotion createPercentPromotion(Long productId,
                                                   ProductPromotionRequest productPromotionRequest) {
        Product product = productRepo.findById(productId).orElse(null);
        validateProductOwnership(product);

        if (productPromotionRequest.startTime().isAfter(productPromotionRequest.endTime())) {
            throw new IllegalArgumentException("Incorrect start and end time");
        }

        ProductPromotion productPromotion = new ProductPromotion(
                product,
                productPromotionRequest.title(),
                productPromotionRequest.discountPercent(),
                productPromotionRequest.startTime().toInstant(),
                productPromotionRequest.endTime().toInstant(),
                productPromotionRequest.active(),
                productPromotionRequest.showEndDate()
        );

        if (productPromotionRepo.existsOverlappingPromotion(
                productId,
                productPromotionRequest.startTime().toInstant(),
                productPromotionRequest.endTime().toInstant()
        )) {
            throw new InvalidStateException("Your dates must not overlap.");
        }

        return productPromotionRepo.save(productPromotion);
    }

    public void deleteProductPromotion(Long promotionId) {
        ProductPromotion productPromotion = productPromotionRepo.findById(promotionId)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "You are not allowed to perform this operation"));

        validateProductOwnership(productPromotion.getProduct());

        productPromotionRepo.deleteById(promotionId);
    }

    public void updateProductPromotion(Long promotionId,
                                       ProductPromotionRequest productPromotionRequest) {
        ProductPromotion productPromotion = productPromotionRepo.findById(promotionId)
                    .orElseThrow(() -> new ForbiddenOperationException(
                        "You are not allowed to perform this operation"));

        validateProductOwnership(productPromotion.getProduct());

        productPromotionMapper.updateProductFromDto(productPromotionRequest, productPromotion);

        productPromotionRepo.save(productPromotion);
    }

    private void validateProductOwnership(Product product) {
        User currentUser = currentUserService.getCurrentUser();

        if (product == null || !product.getSeller().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You are not allowed to perform this operation");
        }
    }

}