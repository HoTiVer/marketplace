package org.hotiver.repo.projection;

import org.hotiver.domain.Entity.ProductPriceHistory;
import org.hotiver.dto.product.ProductPriceHistoryResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductPriceHistoryProjectionRepo
        extends Repository<ProductPriceHistory, Long> {

    @Query("""
    SELECT
        p.product.id,
        p.price,
        p.createdAt
    FROM ProductPriceHistory p
    WHERE p.product.id = :productId
    ORDER BY p.createdAt DESC
""")
    List<ProductPriceHistoryResponse> getProductPriceHistory(@Param("productId") Long productId);
}
