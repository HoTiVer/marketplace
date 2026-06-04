package org.hotiver.repo.projection;

import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.product.SellerInventoryProductDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductProjectionRepo extends Repository<Product, Long> {

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
        WHERE u.id = :userId
    """, nativeQuery = true)
    List<ListProductDto> findUserProductWishListByUserId(Long userId);
}
