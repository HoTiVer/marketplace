package org.hotiver.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ProductReviewPageDto {
    Long productId;
    String productName;
    BigDecimal rating;
    List<ProductReviewDto> productReviews;
}
