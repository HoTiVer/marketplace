package org.hotiver.service;

import org.hotiver.common.OrderStatus;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Review;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.review.ProductReviewDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.dto.review.ReviewPageDto;
import org.hotiver.repo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {

    private final UserRepo userRepo;
    private final ReviewRepo reviewRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final SellerRepo sellerRepo;

    public ReviewService(UserRepo userRepo, ReviewRepo reviewRepo,
                         ProductRepo productRepo, OrderRepo orderRepo, SellerRepo sellerRepo) {
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.sellerRepo = sellerRepo;
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

        if (!orderRepo.isUserBoughtProduct(user.getId(), product.getId(), OrderStatus.COMPLETED.toString())) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto("you should fully buy it before placing comment"));
        }

        Review review = reviewRepo.findReviewByUserIdAndProductId(user.getId(), product.getId());
        if (review != null) {
            review.setComment(reviewDto.getComment());
            review.setRating(reviewDto.getRating());
            review.setUpdatedAt(Date.valueOf(LocalDate.now()));

            reviewRepo.save(review);
            calculateSellerRating(product.getSeller().getId());
            calculateProductRating(product.getId());
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
        calculateSellerRating(product.getSeller().getId());
        calculateProductRating(product.getId());

        return ResponseEntity.ok()
                .body(new ResponseDto("success"));
    }

    private void calculateSellerRating(Long sellerId) {
        BigDecimal averageRating = reviewRepo.calculateSellerRating(sellerId)
                .map(r -> r.setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        Seller seller = sellerRepo.findById(sellerId).orElse(null);

        if (seller != null && averageRating != null) {
            seller.setRating(averageRating);
            sellerRepo.save(seller);
        }
    }

    private void calculateProductRating(Long productId) {
        BigDecimal averageRating = reviewRepo.calculateProductRating(productId)
                .map(r -> r.setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        Product product = productRepo.findById(productId).orElse(null);

        if (product != null && averageRating != null) {
            product.setRating(averageRating);
            productRepo.save(product);
        }
    }

    public ResponseEntity<ReviewPageDto> getProductReview(Long productId) {
        Product product = productRepo.findById(productId).orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }


        List<ProductReviewDto> productReviews = reviewRepo.getProductReview(productId);

        BigDecimal productAverageRating = reviewRepo.calculateProductRating(productId)
                .map(r -> r.setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        ReviewPageDto reviewPageDto = new ReviewPageDto(
                product.getId(),
                product.getName(),
                productAverageRating,
                productReviews);

        return ResponseEntity.ok(reviewPageDto);
    }
}
