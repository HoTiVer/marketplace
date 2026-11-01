package org.hotiver.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDto {
    Long productId;
    String productName;
    BigDecimal price;
    Integer quantity;
    String mainImageUrl;
}
