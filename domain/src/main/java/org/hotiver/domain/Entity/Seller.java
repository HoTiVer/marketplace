package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "seller")
public class Seller  {

    @Id
    private Long id; 

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(unique = true)
    private String nickname;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    private String profileDescription;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "seller")
    private List<Product> products;
}
