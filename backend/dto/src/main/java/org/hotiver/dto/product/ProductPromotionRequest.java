package org.hotiver.dto.product;

import jakarta.validation.constraints.*;
import org.hotiver.dto.validation.Promotion.ValidPromotionTime;

import java.time.OffsetDateTime;

@ValidPromotionTime
public record ProductPromotionRequest(
        @NotNull
        @Max(100)
        @Min(1)
        Integer discountPercent,
        @NotBlank
        @Size(min = 1, max = 100)
        String title,
        @NotNull
        OffsetDateTime startTime,
        @NotNull
        OffsetDateTime endTime,
        @NotNull
        Boolean showEndDate,
        @NotNull
        Boolean active
) { }
