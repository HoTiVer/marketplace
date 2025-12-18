package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-product-image"
    )
    @SequenceGenerator(
            sequenceName = "sequence_product_image",
            name = "sequence-product-image",
            allocationSize = 5
    )
    private Long id;

    private String url;

    private Boolean isMain;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
