package org.hotiver.dto.product;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CurrentSellerProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String categoryName;
    private Map<String, Object> characteristics;
    private Integer quantity;
    private List<ProductImageDto> images;
}