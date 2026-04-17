package org.hotiver.repo;

import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.product.*;
import org.hotiver.dto.product.SellerInventoryProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query(value = """
    SELECT
        p.id as productId,
        p.name as productName,
        p.price as price,
        pi.url AS mainImageUrl
    FROM product p
    LEFT JOIN product_image pi
        ON pi.product_id = p.id AND pi.is_main = true
    WHERE is_visible = true AND p.seller_id = :sellerId
    ORDER BY p.name
    """, nativeQuery = true)
    List<ListProductDto> findAllVisibleBySellerId(@Param("sellerId") Long id);

    @Query(value = """
    SELECT
        p.id as productId,
        p.name as productName,
        p.price as price,
        pi.url AS mainImageUrl
    FROM product p
    LEFT JOIN product_image pi
        ON pi.product_id = p.id AND pi.is_main = true
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
        pi.url AS mainImageUrl
    FROM product p
    JOIN category c ON p.category_id = c.id
    JOIN seller s ON p.seller_id = s.id
    JOIN public."user" u ON s.id = u.id
    LEFT JOIN product_image pi
        ON pi.product_id = p.id AND pi.is_main = true
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

    @Query(value = """
          SELECT
                p.id as productId,
                p.name as productName,
                p.price as price,
                pi.url AS mainImageUrl
          FROM product p
          LEFT JOIN product_image pi
            ON pi.product_id = p.id AND pi.is_main = true
          WHERE is_visible = true
          ORDER BY random()
          LIMIT :limit
          """, nativeQuery = true
    )
    List<ListProductDto> findRandomVisibleProducts(
            @Param("limit") Integer limitForFeaturesProducts);

    @Query(value = """
          SELECT
                p.id as productId,
                p.name as productName,
                p.price as price,
                pi.url AS mainImageUrl
          FROM product p
          LEFT JOIN product_image pi
            ON pi.product_id = p.id AND pi.is_main = true
          WHERE is_visible = true
          ORDER BY p.publishing_date DESC
          LIMIT :limit
          """, nativeQuery = true
    )
    List<ListProductDto> findNewestVisibleProducts(
            @Param("limit") Integer limitForNewProducts);

    @Query(value = """
          SELECT
                p.id as productId,
                p.name as productName,
                p.price as price,
                pi.url AS mainImageUrl
          FROM product p
          LEFT JOIN product_image pi
            ON pi.product_id = p.id AND pi.is_main = true
          WHERE is_visible = true
          ORDER BY p.sales_count DESC
          LIMIT :limit
          """, nativeQuery = true
    )
    List<ListProductDto> findPopularVisibleProducts(
            @Param("limit") Integer limitForPopularProducts);

    @Query("""
    SELECT
        p.id,
        p.name,
        p.price,
        p.stockQuantity,
        pi.url
    FROM Product p
    LEFT JOIN ProductImage pi
        ON pi.product = p AND pi.isMain = true
    WHERE p.seller.id = :id
      AND p.isVisible = true
    ORDER BY p.name
""")
    List<SellerInventoryProductDto> getCurrentSellerProducts(@Param("id") Long id);

    @Query("""
    SELECT
        p.id AS id,
        p.name AS name,
        p.price AS price,
        p.description AS description,
        p.category.name AS categoryName,
        p.stockQuantity AS quantity,
        COALESCE(pi.url, 'default.png') AS mainImageUrl
    FROM Product p
    LEFT JOIN ProductImage pi
        ON pi.product = p AND pi.isMain = true
    WHERE p.seller.id = :sellerId
      AND p.id = :productId
      AND p.isVisible = true
""")
    SellerInventoryProductDto getCurrentSellerProductById(Long sellerId, Long productId);

    @Query(value = """
        SELECT
            p.id as productId,
            p.name as productName,
            p.price as price,
            pi.url as mainImageUrl
        FROM public."user" u
        JOIN user_wishes uw ON uw.user_id = u.id
        JOIN product p ON p.id = uw.product_id
        LEFT JOIN product_image pi ON pi.product_id = p.id AND pi.is_main = true
        WHERE u.email = :email
    """, nativeQuery = true)
    List<ListProductDto> findUserProductWishList(String email);
}
