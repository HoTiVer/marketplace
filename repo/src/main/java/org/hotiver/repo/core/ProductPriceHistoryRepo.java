package org.hotiver.repo.core;

import org.hotiver.domain.Entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPriceHistoryRepo
        extends JpaRepository<ProductPriceHistory, Long> {
}
