package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "category")
public class Category {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-category")
    @SequenceGenerator(
            name = "sequence-category",
            sequenceName = "sequence_category",
            allocationSize = 5
    )
    private Long id;

    @Column(unique = true)
    private String name;

}
