package org.hotiver.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDto {
    Long productId;
    String productName;
    Double price;
    Integer quantity;
}
