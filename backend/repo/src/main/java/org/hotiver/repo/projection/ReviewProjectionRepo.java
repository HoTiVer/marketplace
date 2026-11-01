package org.hotiver.repo.projection;

import org.hotiver.domain.Entity.Review;
import org.hotiver.dto.review.ProductReviewDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ReviewProjectionRepo extends Repository<Review, Long> {

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
    List<ProductReviewDto> getProductReviews(Long productId);

}
