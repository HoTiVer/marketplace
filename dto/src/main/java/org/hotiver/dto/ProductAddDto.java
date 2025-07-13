package org.hotiver.dto;

import lombok.Getter;
import lombok.Setter;
import org.hotiver.common.ProductCategory;

import java.util.Map;

@Getter
@Setter
public class ProductAddDto {
    private String name;

    private Double price;

    private String description;

    private ProductCategory category;

    private Map<String, String> characteristic;
}
