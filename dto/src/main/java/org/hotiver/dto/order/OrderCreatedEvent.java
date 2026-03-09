package org.hotiver.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;

    private Long productId;
    private String productName;

    private Long categoryId;
    private String categoryName;

    private Long sellerId;

    private Integer productQuantity;
    private BigDecimal totalAmount;
    private Date createdAt;
}