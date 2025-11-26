package org.hotiver.repo;

import org.hotiver.domain.Entity.Order;
import org.hotiver.dto.order.SellerOrderDto;
import org.hotiver.dto.order.UserOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepo extends JpaRepository<Order, Long> {


    @Query(value = """
        SELECT
                o.id as orderId,
                p.id as productId,
                p.name as productName,
                s.nickname as sellerNickname,
                o.quantity as quantity,
                o.order_date as orderDate,
                o.delivery_date as deliveryDate,
                o.status as orderStatus,
                o.total_price as totalPrice,
                o.delivery_address as deliveryAddress
        FROM public."order" o
        JOIN public."user" u on u.id = o.user_id
        JOIN public.product p on p.id = o.product_id
        JOIN public.seller s on s.id = p.seller_id
        WHERE o.user_id = :id
        ORDER BY o.order_date DESC, o.product_id ASC
    """, nativeQuery = true)
    Page<UserOrderDto> findUserOrders(@Param("id") Long userId, Pageable pageable);

    @Query(value = """
        SELECT
                o.id as orderId,
                p.id as productId,
                p.name as productName,
                o.quantity as quantity,
                o.order_date as orderDate,
                o.delivery_date as deliveryDate,
                o.status as orderStatus,
                o.total_price as totalPrice,
                o.delivery_address as deliveryAddress,
                o.delivery_city as deliveryCity,
                o.recipient_name as recipientName,
                o.recipient_phone as recipientPhone
        FROM public."order" o
        JOIN public."user" u on u.id = o.user_id
        JOIN public.product p on p.id = o.product_id
        JOIN public.seller s on s.id = p.seller_id
        WHERE o.seller_id = :id
        ORDER BY o.order_date DESC, o.product_id ASC
    """, nativeQuery = true)
    Page<SellerOrderDto> findSellerOrders(@Param("id") Long sellerId, Pageable pageable);

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
                                @Param("orderStatus") String status);
}
