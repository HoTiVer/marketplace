package org.hotiver.api.Controller;

import jakarta.validation.Valid;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.dto.review.ReviewPageDto;
import org.hotiver.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/product/{productId}/review")
    public ResponseEntity<ResponseDto> addReviewToProduct(@RequestBody @Valid ReviewDto reviewDto,
                                                          @PathVariable Long productId){

        ResponseDto response = reviewService.addReviewToProduct(reviewDto, productId);
        if (response == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto("you should fully buy it before placing comment"));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}/review")
    public ResponseEntity<ReviewPageDto> getProductReviews(@PathVariable Long productId){
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
}
