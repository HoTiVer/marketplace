package org.hotiver.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListProductDto {
    Long productId;
    String productName;
    BigDecimal price;
    String mainImageUrl;
}
