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
        boolean active,
        boolean showEndDate
) {}
