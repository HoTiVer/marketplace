package org.hotiver.dto.product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductPromotionDto(
    boolean isPromotion,
    BigDecimal defaultPrice,
    BigDecimal promotionPrice,
    OffsetDateTime promotionEndTime
) {}
