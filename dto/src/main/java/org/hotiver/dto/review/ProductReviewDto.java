package org.hotiver.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class ProductReviewDto {
    private Long reviewId;
    private Long productId;
    private String commentatorName;
    private String comment;
    private Integer rating;
    private Date updatedAt;

    public ProductReviewDto(Long reviewId, Long productId,
                            String commentatorName, String comment,
                            Integer rating, Date updatedAt) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.commentatorName = commentatorName;
        this.comment = comment;
        this.rating = rating;
        this.updatedAt = updatedAt;
    }
}
