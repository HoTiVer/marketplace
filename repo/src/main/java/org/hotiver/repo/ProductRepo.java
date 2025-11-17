package org.hotiver.repo;

import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.product.*;
import org.hotiver.dto.seller.SellerProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.seller.id = :id AND p.isVisible = true")
    List<Product> findAllVisibleBySellerId(Long id);

    @Query("""
    SELECT\s
        p.id AS id,
        p.name AS name,
        p.price AS price,
        p.description AS description,
        p.category.name AS categoryName,
        p.characteristic AS characteristic,
        p.seller.user.displayName AS sellerDisplayName,
        p.seller.nickname AS sellerUsername
    FROM Product p
    WHERE p.category.name = :category AND p.isVisible = true
""")
    Page<ProductProjection> findByCategory(String category, Pageable pageable);

    @Query(value = """
    SELECT
        p.id AS id,
        p.name AS name,
        p.price AS price,
        p.description AS description,
        c.name AS categoryName,
        p.characteristic AS characteristic,
        u.display_name AS sellerDisplayName,
        s.nickname AS sellerUsername
    FROM product p
    JOIN category c ON p.category_id = c.id
    JOIN seller s ON p.seller_id = s.id
    JOIN public."user" u ON s.id = u.id
    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
       OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
       OR EXISTS (
           SELECT 1
           FROM jsonb_each_text(p.characteristic) AS kv
           WHERE kv.value ILIKE CONCAT('%', :searchTerm, '%')
       )
""", nativeQuery = true)
    List<ProductSearchDto> findByKeyWord(@Param("searchTerm") String searchTerm);

    @Query(value = """
          SELECT
                p.id as productId,
                p.name as productName,
                p.price as price
          FROM product p
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
                p.price as price
          FROM product p
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
                p.price as price
          FROM product p
          WHERE is_visible = true
          ORDER BY p.sales_count DESC
          LIMIT :limit
          """, nativeQuery = true
    )
    List<ListProductDto> findPopularVisibleProducts(
            @Param("limit") Integer limitForPopularProducts);

    @Query("""
    SELECT\s
        p.id AS id,
        p.name AS name,
        p.price AS price,
        p.description AS description,
        p.category.name AS categoryName,
        p.characteristic AS characteristic,
        p.seller.user.displayName AS sellerDisplayName,
        p.seller.nickname AS sellerUsername,
        p.stockQuantity as quantity
    FROM Product p
    WHERE p.seller.id = :id AND p.isVisible = true
""")
    List<SellerProductProjection> getCurrentSellerProducts(Long id);
}
