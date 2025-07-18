package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "sellers")
public class Seller  {

    @Id
    private Long id; 

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(unique = true)
    private String nickname;

    private Double rating;

    private String profileDescription;

}
