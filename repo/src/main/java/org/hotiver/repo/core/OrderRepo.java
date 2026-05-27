package org.hotiver.repo.core;

import org.hotiver.domain.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepo extends JpaRepository<Order, Long> {

    @Query(value = """
        SELECT EXISTS(
                SELECT 1
                FROM public."order" o
                WHERE o.user_id = :userId
                        AND o.product_id = :productId
                        AND o.status = :orderStatus
        )
    """, nativeQuery = true)
    boolean isUserBoughtProduct(@Param("userId") Long userId,
                                @Param("productId") Long productId,
                                @Param("orderStatus") String status
    );
}
