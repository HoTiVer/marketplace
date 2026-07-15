package org.hotiver.service.product;


import lombok.AllArgsConstructor;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.ProductPriceHistory;
import org.hotiver.dto.product.ProductPriceHistoryResponse;
import org.hotiver.repo.core.ProductPriceHistoryRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.projection.ProductPriceHistoryProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductPriceHistoryService {

    private final ProductPriceHistoryProjectionRepo productPriceHistoryProjectionRepo;
    private final ProductPriceHistoryRepo productPriceHistoryRepo;
    private final CurrentUserService currentUserService;
    private final ProductRepo productRepo;

    public List<ProductPriceHistoryResponse> getProductPriceHistory(Long productId) {
        validateProductOwnership(productId);

        return productPriceHistoryProjectionRepo.getProductPriceHistory(productId);
    }

    @Transactional
    public void addProductPriceHistory(Product product) {
        ProductPriceHistory productPriceHistory = new ProductPriceHistory(
                product,
                product.getPrice(),
                LocalDateTime.now()
        );

        productPriceHistoryRepo.save(productPriceHistory);
    }

    private void validateProductOwnership(Long productId) {
        var currentUser = currentUserService.getCurrentUser();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "You are not allowed to perform this operation")
                );

        if (!product.getSeller().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You are not allowed to perform this operation");
        }
    }

}
