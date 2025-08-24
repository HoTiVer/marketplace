package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;
import netscape.javascript.JSObject;
import org.hibernate.annotations.Type;
import org.hotiver.common.ProductCategory;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double price;

    private String description;

    @Enumerated(value = EnumType.STRING)
    private ProductCategory category;

    @Column(columnDefinition = "jsonb")
    private String characteristic;

    @ManyToOne
    private Seller seller;

    private Boolean isVisible;
}
