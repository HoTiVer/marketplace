package org.hotiver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
public class SellerInventoryProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String mainImageUrl;
}
