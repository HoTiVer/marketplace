package org.hotiver.dto.product;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductGetDto {
    private Long id;

    private String name;

    private Double price;

    private String description;

    private String categoryName;

    private Map<String, Object> characteristic;

    private String sellerDisplayName;

    private String sellerUsername;
}
