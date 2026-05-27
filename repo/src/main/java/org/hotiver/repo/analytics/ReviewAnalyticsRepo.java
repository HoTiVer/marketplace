package org.hotiver.repo.analytics;

import org.hotiver.domain.Entity.Review;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.util.Optional;

public interface ReviewAnalyticsRepo extends Repository<Review, Long> {
    @Query(value = """
    SELECT AVG(r.rating)
    FROM review r
    JOIN public.product p on p.id = r.product_id
    JOIN public.seller s on s.id = p.seller_id
    WHERE s.id = :sellerId
    """, nativeQuery = true)
    Optional<BigDecimal> calculateSellerRating(Long sellerId);

    @Query(value = """
    SELECT AVG(r.rating)
    FROM review r
    JOIN public.product p on p.id = r.product_id
    WHERE p.id = :productId
    """, nativeQuery = true)
    Optional<BigDecimal> calculateProductRating(Long productId);
}
