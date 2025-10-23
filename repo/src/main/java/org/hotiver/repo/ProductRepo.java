package org.hotiver.repo;

import org.hotiver.domain.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.seller.id = :id AND p.isVisible = true")
    List<Product> findAllVisibleBySellerId(Long id);
}
