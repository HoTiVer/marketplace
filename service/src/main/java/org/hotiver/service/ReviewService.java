package org.hotiver.service;

import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Review;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.repo.OrderRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.ReviewRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;

@Service
public class ReviewService {

    private final UserRepo userRepo;
    private final ReviewRepo reviewRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;

    public ReviewService(UserRepo userRepo, ReviewRepo reviewRepo,
                         ProductRepo productRepo, OrderRepo orderRepo) {
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    public ResponseEntity<ResponseDto> addReviewToProduct(ReviewDto reviewDto, Long id) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        Product product = productRepo.findById(id).orElse(null);
        if  (product == null) {
            return ResponseEntity.notFound().build();
        }

        if (reviewDto.getComment() != null) {
            if (reviewDto.getComment().length() > 500) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("comment must be less than 500 characters"));
            }
        }

        if (reviewDto.getRating() == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto("rating is required"));
        }

        if (reviewDto.getRating() < 1 || reviewDto.getRating() > 5) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto("rating must be between 1 and 5"));
        }

        if (!orderRepo.isUserBoughtProduct(user.getId(), product.getId())) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto("you should buy it before placing comment"));
        }

        Review review = reviewRepo.findReviewByUserIdAndProductId(user.getId(), product.getId());
        if (review != null) {
            review.setComment(reviewDto.getComment());
            review.setRating(reviewDto.getRating());
            review.setUpdatedAt(Date.valueOf(LocalDate.now()));

            reviewRepo.save(review);
            return ResponseEntity.ok(new ResponseDto("success"));
        }

        review = Review.builder()
                .product(product)
                .user(user)
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .createdAt(Date.valueOf(LocalDate.now()))
                .updatedAt(Date.valueOf(LocalDate.now()))
                .build();

        reviewRepo.save(review);

        return ResponseEntity.ok()
                .body(new ResponseDto("success"));
    }
}
