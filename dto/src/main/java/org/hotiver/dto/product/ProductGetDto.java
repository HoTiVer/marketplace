package org.hotiver.dto.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ProductGetDto {
    private String name;

    private Double price;

    private String description;

    private String categoryName;

    private Map<String, String> characteristic;

    private String sellerDisplayName;

    private String sellerUsername;
}
