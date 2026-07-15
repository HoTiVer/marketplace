package org.hotiver.domain.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_price_history")
public class ProductPriceHistory {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-product-price-history"
    )
    @SequenceGenerator(
            sequenceName = "sequence_product_price_history",
            name = "sequence-product-price-history",
            allocationSize = 5
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private BigDecimal price;

    private LocalDateTime createdAt;

    public ProductPriceHistory(Product product, BigDecimal price, LocalDateTime createdAt) {
        this.product = product;
        this.price = price;
        this.createdAt = createdAt;
    }
}
