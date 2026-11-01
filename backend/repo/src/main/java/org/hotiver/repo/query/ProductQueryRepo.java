package org.hotiver.repo.query;

import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.product.ListProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ProductQueryRepo extends Repository<Product, Long> {

    @Query(value = """
    SELECT
        p.id as productId,
        p.name as productName,
        p.price as price,
        pi.url AS mainImageUrl,
        pp.discount_percent as promotionValue
    FROM product p
    LEFT JOIN product_image pi
        ON pi.product_id = p.id AND pi.is_main = true
    LEFT JOIN product_promotion pp
        ON pp.product_id = p.id
            AND pp.active
            AND pp.start_time <= NOW()
            AND pp.end_time >= NOW()
    JOIN category c ON c.id = p.category_id
    WHERE c.name = :category AND p.is_visible = true
    """,
            countQuery = """
          SELECT COUNT(*)
    FROM Product p
    JOIN category c ON c.id = p.category_id
    WHERE p.is_visible = true AND c.name = :category
    """, nativeQuery = true)
    Page<ListProductDto> findByCategory(String category, Pageable pageable);

    @Query(value = """
    SELECT
        p.id as productId,
        p.name as productName,
        p.price as price,
        pi.url AS mainImageUrl,
        pp.discount_percent as promotionValue
    FROM product p
    JOIN category c ON p.category_id = c.id
    JOIN seller s ON p.seller_id = s.id
    JOIN public."user" u ON s.id = u.id
    LEFT JOIN product_image pi
        ON pi.product_id = p.id AND pi.is_main = true
    LEFT JOIN product_promotion pp
        ON pp.product_id = p.id
            AND pp.active
            AND pp.start_time <= NOW()
            AND pp.end_time >= NOW()
    WHERE p.is_visible = true
      AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR EXISTS (
                SELECT 1
                FROM jsonb_each_text(p.characteristic) AS kv
                WHERE kv.value ILIKE CONCAT('%', :searchTerm, '%')
            )
      )
    """, countQuery = """
    SELECT COUNT(*)
    FROM product p
    WHERE p.is_visible = true
      AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR EXISTS (
                SELECT 1
                FROM jsonb_each_text(p.characteristic) AS kv
                WHERE kv.value ILIKE CONCAT('%', :searchTerm, '%')
            )
      )
""", nativeQuery = true)
    Page<ListProductDto> findByKeyWord(@Param("searchTerm") String searchTerm, Pageable pageable);

}
