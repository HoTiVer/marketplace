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
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private RoleType roleType;
}
