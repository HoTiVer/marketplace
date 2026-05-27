package org.hotiver.repo.projection;

import org.hotiver.domain.Entity.CartItem;
import org.hotiver.dto.cart.CartItemDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CartItemProjectionRepo extends Repository<CartItem, Long> {
    @Query(value = """
    SELECT
            ci.product_id as productId,
            p.name as productName,
            p.price as price,
            ci.quantity as quantity,
            pi.url as mainImageUrl
        FROM public.cart_item ci
        JOIN public.product p on p.id = ci.product_id
        LEFT JOIN public.product_image pi ON p.id = pi.product_id
        WHERE ci.user_id = :userId
        ORDER BY p.name ASC
    """, nativeQuery = true)
    List<CartItemDto> findByUserId(Long userId);
}
