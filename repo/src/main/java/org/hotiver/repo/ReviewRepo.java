package org.hotiver.repo;

import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Review;
import org.hotiver.dto.review.ProductReviewDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepo extends JpaRepository<Review, Long> {

//    @Query(value = """
//        SELECT EXISTS(
//                SELECT 1
//                FROM public.review r
//                WHERE r.user_id = :userId AND r.product_id = :productId
//            );
//        """, nativeQuery = true)
//    boolean isUserReviewedProduct(Long userId, Long productId);

    @Query(value = """
    SELECT *
    FROM public.review r
    WHERE r.user_id = :userId AND r.product_id = :productId
    """, nativeQuery = true)
    Review findReviewByUserIdAndProductId(Long userId, Long productId);

    @Query(value = """
    SELECT
            r.id as "reviewId",
            r.product_id as "productId",
            u.display_name as "commentatorName",
            r.comment as comment,
            r.rating as rating,
            r.updated_at as "updatedAt"
    FROM public.review r
    JOIN public."user" u on u.id = r.user_id
    WHERE r.product_id = :productId
    """, nativeQuery = true)
    List<ProductReviewDto> getProductReview(Long productId);

    @Query(value = """
    SELECT AVG(r.rating)
    FROM review r
    JOIN public.product p on p.id = r.product_id
    JOIN public.seller s on s.id = p.seller_id
    WHERE s.id = :sellerId
    """, nativeQuery = true)
    Optional<BigDecimal> calculateSellerRating(Long sellerId);
}
