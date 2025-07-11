package org.hotiver.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hotiver.common.RoleType;
import org.springframework.beans.factory.annotation.Value;


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
