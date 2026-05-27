package org.hotiver.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.OrderStatus;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Review;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.review.ProductReviewDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.dto.review.ProductReviewPageDto;
import org.hotiver.repo.analytics.ReviewAnalyticsRepo;
import org.hotiver.repo.core.OrderRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.ReviewRepo;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.repo.projection.ReviewProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductReviewService {

    private final ReviewRepo reviewRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final SellerRepo sellerRepo;
    private final CurrentUserService currentUserService;
    private final ReviewProjectionRepo reviewProjectionRepo;
    private final ReviewAnalyticsRepo reviewAnalyticsRepo;

    public ProductReviewService(ReviewRepo reviewRepo, ProductRepo productRepo,
                                OrderRepo orderRepo, SellerRepo sellerRepo,
                                CurrentUserService currentUserService,
                                ReviewProjectionRepo reviewProjectionRepo,
                                ReviewAnalyticsRepo reviewAnalyticsRepo) {
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.sellerRepo = sellerRepo;
        this.currentUserService = currentUserService;
        this.reviewProjectionRepo = reviewProjectionRepo;
        this.reviewAnalyticsRepo = reviewAnalyticsRepo;
    }

    public void addReviewToProduct(ReviewDto reviewDto, Long id) {
        User user = currentUserService.getCurrentUser();

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!orderRepo.isUserBoughtProduct(user.getId(),
                product.getId(), OrderStatus.COMPLETED.toString())) {
            throw new ForbiddenOperationException("You should fully buy it before placing comment");
        }

        Review review = reviewRepo.findReviewByUserIdAndProductId(user.getId(), product.getId())
                .orElse(null);
        if (review != null)
            updateReview(reviewDto, review, product);
        else
            review = createReview(reviewDto, user, product);

        reviewRepo.save(review);
        calculateSellerRating(product.getSeller().getId());
        calculateProductRating(product.getId());
    }

    private Review createReview(ReviewDto reviewDto, User user, Product product) {
        return Review.builder()
                .product(product)
                .user(user)
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .createdAt(Date.valueOf(LocalDate.now()))
                .updatedAt(Date.valueOf(LocalDate.now()))
                .build();
    }

    private void updateReview(ReviewDto reviewDto, Review review, Product product) {
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setUpdatedAt(Date.valueOf(LocalDate.now()));
    }

    private void calculateSellerRating(Long sellerId) {
        BigDecimal averageRating = reviewAnalyticsRepo.calculateSellerRating(sellerId)
                .map(r -> r.setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        Seller seller = sellerRepo.findById(sellerId).orElse(null);

        if (seller != null && averageRating != null) {
            seller.setRating(averageRating);
            sellerRepo.save(seller);
        }
    }

    private void calculateProductRating(Long productId) {
        BigDecimal averageRating = reviewAnalyticsRepo.calculateProductRating(productId)
                .map(r -> r.setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        Product product = productRepo.findById(productId).orElse(null);

        if (product != null && averageRating != null) {
            product.setRating(averageRating);
            productRepo.save(product);
        }
    }

    public ProductReviewPageDto getProductReviews(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(()-> new EntityNotFoundException("Product not found"));


        List<ProductReviewDto> productReviews = reviewProjectionRepo.getProductReviews(productId);

        BigDecimal productAverageRating = reviewAnalyticsRepo.calculateProductRating(productId)
                .map(r -> r.setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        return new ProductReviewPageDto(
                product.getId(),
                product.getName(),
                productAverageRating,
                productReviews);
    }
}
