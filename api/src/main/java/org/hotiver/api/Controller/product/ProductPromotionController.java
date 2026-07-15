package org.hotiver.api.Controller.product;

import jakarta.validation.Valid;
import org.hotiver.domain.Entity.ProductPromotion;
import org.hotiver.dto.product.ProductPromotionRequest;
import org.hotiver.dto.product.SellerProductPromotionDto;
import org.hotiver.service.product.ProductPromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductPromotionController {

    private final ProductPromotionService productPromotionService;

    public ProductPromotionController(ProductPromotionService productPromotionService) {
        this.productPromotionService = productPromotionService;
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{productId}/promotions")
    public ResponseEntity<Void> addProductPromotion(
            @PathVariable Long productId,
            @Valid @RequestBody ProductPromotionRequest productPromotionRequest) {
        ProductPromotion createPromotion = productPromotionService
                .createPercentPromotion(productId, productPromotionRequest);
        URI location =
                URI.create("/api/v1/promotions/" + createPromotion.getId());

        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{productId}/promotions")
    public ResponseEntity<List<SellerProductPromotionDto>> getProductPromotions(
            @PathVariable Long productId) {
        return ResponseEntity.ok().body(productPromotionService.getProductPromotions(productId));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/promotions/{promotionId}")
    public ResponseEntity<Void> updateProductPromotion(
            @PathVariable Long promotionId,
            @Valid @RequestBody ProductPromotionRequest productPromotionRequest) {
        productPromotionService.updateProductPromotion(promotionId, productPromotionRequest);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/promotions/{promotionId}")
    public ResponseEntity<Void> deleteProductPromotion(@PathVariable Long promotionId) {
        productPromotionService.deleteProductPromotion(promotionId);
        return ResponseEntity.noContent().build();
    }
}
