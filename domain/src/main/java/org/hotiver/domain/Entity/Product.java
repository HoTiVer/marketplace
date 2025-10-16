package org.hotiver.domain.Entity;


import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

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

    private Double price;

    private String description;

    @OneToOne
    @Enumerated(value = EnumType.STRING)
    private Category category;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> characteristic;

    @ManyToOne
    private Seller seller;

    private Boolean isVisible;
}
