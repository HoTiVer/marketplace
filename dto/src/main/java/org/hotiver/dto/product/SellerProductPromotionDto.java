package org.hotiver.dto.product;

import java.time.OffsetDateTime;

public record SellerProductPromotionDto (
        Long promotionId,
        Long productId,
        String productName,
        String title,
        Integer discountPercent,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Boolean active,
        Boolean showEndDate
) {}
