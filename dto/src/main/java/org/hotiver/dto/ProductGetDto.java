package org.hotiver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hotiver.common.ProductCategory;

import java.util.Map;

@Getter
@Setter
@Builder
public class ProductGetDto {
    private String name;

    private Double price;

    private String description;

    private ProductCategory category;

    private Map<String, String> characteristic;

    private String sellerDisplayName;

    private String sellerUsername;
}
