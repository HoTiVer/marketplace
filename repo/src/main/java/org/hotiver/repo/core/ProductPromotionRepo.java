package org.hotiver.repo.core;

import org.hotiver.domain.Entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductPromotionRepo
        extends JpaRepository<ProductPromotion, Long> {

    @Query("""
    SELECT COUNT(p) > 0
    FROM ProductPromotion p
    WHERE p.product.id = :productId
      AND p.startTime <= :endTime
      AND p.endTime >= :startTime
    """)
    boolean existsOverlappingPromotion(
            @Param("productId") Long productId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Query("""
    SELECT p
    FROM ProductPromotion p
    WHERE p.product.id = :productId
        AND p.startTime <= :currentTime
        AND p.endTime >= :currentTime
        AND p.active = true
    """)
    Optional<ProductPromotion> findActiveProductPromotion(
            @Param("productId") Long productId,
            @Param("currentTime") Instant currentTime
    );

    @Query("""
    SELECT pp
    FROM ProductPromotion pp
    WHERE pp.product.id = :productId
    ORDER BY pp.active, pp.product.name
    """)
    List<ProductPromotion> findProductPromotions(Long productId);
}
