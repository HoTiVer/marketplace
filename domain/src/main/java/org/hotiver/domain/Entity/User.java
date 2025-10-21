package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user", schema = "public")
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user-sequence"
    )
    @SequenceGenerator(
            name = "user-sequence",
            sequenceName = "sequence_user",
            allocationSize = 5
    )
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private Double balance;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    private String displayName;

    private Date registerDate;

    @ManyToMany
    @JoinTable(
            name = "user_wishes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> wishlist = new HashSet<>();

    private Boolean isTwoFactorEnable;
}
