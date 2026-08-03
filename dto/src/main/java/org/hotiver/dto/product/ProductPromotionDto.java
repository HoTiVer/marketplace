package org.hotiver.dto.product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductPromotionDto(
    Boolean isPromotion,
    BigDecimal defaultPrice,
    BigDecimal promotionPrice,
    OffsetDateTime promotionEndTime
) {}
