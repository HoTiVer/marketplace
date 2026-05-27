package org.hotiver.repo.core;

import org.hotiver.domain.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    @Query(value = """
    SELECT *
    FROM public.review r
    WHERE r.user_id = :userId AND r.product_id = :productId
    """, nativeQuery = true)
    Optional<Review> findReviewByUserIdAndProductId(Long userId, Long productId);
}
