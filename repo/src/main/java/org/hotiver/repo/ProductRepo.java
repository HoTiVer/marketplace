package org.hotiver.repo;

import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.product.ProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}
