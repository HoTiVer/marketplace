package org.hotiver.api;

import jakarta.validation.Valid;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.review.ProductReviewDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/product/{productId}/review")
    public ResponseEntity<ResponseDto> addReviewToProduct(@RequestBody ReviewDto reviewDto,
                                                          @PathVariable Long productId){

        return reviewService.addReviewToProduct(reviewDto, productId);
    }

    @GetMapping("/product/{productId}/review")
    public ResponseEntity<List<ProductReviewDto>> getProductReview(@PathVariable Long productId){
        return reviewService.getProductReview(productId);
    }
}
