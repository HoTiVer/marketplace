package org.hotiver.api.Controller;

import org.hotiver.service.product.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PreAuthorize("hasRole('SELLER')")
    @PatchMapping("/product/{productId}/image/{imageId}/main")
    public ResponseEntity<Void> makeProductMainImage(@PathVariable Long productId,
                                                     @PathVariable Long imageId) {
        productImageService.makeProductMainImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/product/{productId}/image/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Long productId,
                                                   @PathVariable Long imageId) {
        productImageService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

}
