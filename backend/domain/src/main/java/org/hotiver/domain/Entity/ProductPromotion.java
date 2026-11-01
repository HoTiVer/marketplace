package org.hotiver.domain.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_promotion")
public class ProductPromotion {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-product-promotion"
    )
    @SequenceGenerator(
            sequenceName = "sequence_product_promotion",
            name = "sequence-product-promotion",
            allocationSize = 5
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(length = 100)
    private String title;

    private Integer discountPercent;

    private Instant startTime;

    private Instant endTime;

    private boolean active;

    private boolean showEndDate;

    public ProductPromotion(Product product, String title, Integer discountPercent,
                            Instant startTime, Instant endTime,
                            boolean active, boolean showEndDate) {
        this.product = product;
        this.title = title;
        this.discountPercent = discountPercent;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
        this.showEndDate = showEndDate;
    }
}
