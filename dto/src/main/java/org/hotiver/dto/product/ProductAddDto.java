package org.hotiver.dto.product;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ProductAddDto {
    @NotBlank(message = "Name must be assigned")
    private String name;

    @NotNull(message = "Price must be assigned")
    @Positive(message = "Price must be a positive value")
    private BigDecimal price;

    @NotNull(message = "Description must be assigned")
    private String description;

    @NotBlank(message = "Category must be assigned")
    private String categoryName;

    @NotNull
    private Map<String, Object> characteristics;

    @NotNull(message = "Quantity must be assigned")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;
}
