package org.hotiver.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ReviewPageDto {
    Long productId;
    String productName;
    List<ProductReviewDto> productReviews;
}
