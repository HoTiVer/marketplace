package org.hotiver.domain.Entity;


import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hotiver.common.Exception.base.InvalidStateException;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-product"
    )
    @SequenceGenerator(
            name = "sequence-product",
            sequenceName = "sequence_product",
            allocationSize = 5
    )
    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    @OneToOne
    @Enumerated(value = EnumType.STRING)
    private Category category;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> characteristic;

    @ManyToOne
    private Seller seller;

    private Integer stockQuantity;

    private Integer salesCount;

    private Date publishingDate;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    private Boolean isVisible;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    public void addProductImage(ProductImage productImage) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(productImage);
    }

    public void sell(Integer quantityToBuy) {
        if (quantityToBuy <= 0) {
            throw new InvalidStateException("Quantity must be greater than zero.");
        }
        if (quantityToBuy > stockQuantity) {
            throw new InvalidStateException("Buying quantity is greater than stock quantity");
        }

        salesCount += quantityToBuy;
        stockQuantity -= quantityToBuy;
    }

    public void getBack(Integer quantityToBack) {
        salesCount -= quantityToBack;
        stockQuantity += quantityToBack;
    }
}
