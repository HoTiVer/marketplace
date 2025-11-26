package org.hotiver.api;

import jakarta.validation.Valid;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/product/{id}/review")
    public ResponseEntity<ResponseDto> addReviewToProduct(@RequestBody ReviewDto reviewDto,
                                                          @PathVariable Long id){

        return reviewService.addReviewToProduct(reviewDto, id);
    }
}
