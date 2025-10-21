package org.hotiver.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProductAddDto {
    private String name;

    private Double price;

    private String description;

    private String categoryName;

    private Map<String, Object> characteristic;
}
