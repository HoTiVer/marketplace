package org.hotiver.dto.product;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductSearchDto {
    private Long id;

    private String name;

    private Double price;

    private String description;

    private String categoryName;

    private String characteristic;

    private String sellerDisplayName;

    private String sellerUsername;
}
