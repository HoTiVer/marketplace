package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hotiver.common.SellerRegisterRequestStatus;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "register_seller_request")
public class SellerRegister {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-register_seller_request"
    )
    @SequenceGenerator(
            name = "sequence-register_seller_request",
            sequenceName = "sequence_register_seller_request",
            allocationSize = 5
    )
    private Long id;

    private Long userId;

    private Date requestDate;

    private String requestedNickname;

    private String displayName;

    private String profileDescription;

    @Enumerated(EnumType.STRING)
    private SellerRegisterRequestStatus status;
}
