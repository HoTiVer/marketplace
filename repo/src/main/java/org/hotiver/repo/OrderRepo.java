package org.hotiver.repo;

import org.hotiver.domain.Entity.Order;
import org.hotiver.dto.order.UserOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepo extends JpaRepository<Order, Long> {


    @Query(value = """
        SELECT
                o.id as orderId,
                p.id as productId,
                s.id as sellerId,
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
        WHERE u.id = :id
        ORDER BY o.order_date DESC, o.product_id ASC
    """, nativeQuery = true)
    Page<UserOrderDto> findUserOrders(Long id, Pageable pageable);
}
