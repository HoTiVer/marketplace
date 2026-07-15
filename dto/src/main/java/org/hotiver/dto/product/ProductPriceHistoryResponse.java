package org.hotiver.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductPriceHistoryResponse(
    Long productId,
    BigDecimal price,
    LocalDateTime createdAt
) { }
