package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hotiver.common.RoleType;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-role"
    )
    @SequenceGenerator(
            name = "sequence-role",
            sequenceName = "sequence_role",
            allocationSize = 5
    )
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(unique = true)
    private RoleType name;
}
