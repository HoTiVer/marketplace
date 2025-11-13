package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hotiver.common.OrderStatus;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order", schema = "public")
public class Order {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-order"
    )
    @SequenceGenerator(
            sequenceName = "sequence_order",
            name = "sequence-order",
            allocationSize = 5
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    private Integer quantity;

    private Date orderDate;
    private Date deliveryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Double totalPrice;

    private String deliveryAddress;
    private String deliveryCity;
    private String receiverName;
    private String receiverPhone;

}
