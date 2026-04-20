package org.hotiver.api.Controller;

import jakarta.validation.Valid;
import org.hotiver.dto.review.ProductReviewResponse;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.dto.review.ProductReviewPageDto;
import org.hotiver.service.product.ProductReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ProductReviewService productReviewService;

    public ReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/product/{productId}/review")
    public ResponseEntity<ProductReviewResponse> addReviewToProduct(@RequestBody @Valid ReviewDto reviewDto,
                                                                    @PathVariable Long productId){

        productReviewService.addReviewToProduct(reviewDto, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}/review")
    public ResponseEntity<ProductReviewPageDto> getProductReviews(@PathVariable Long productId){
        return ResponseEntity.ok(productReviewService.getProductReviews(productId));
    }
}
