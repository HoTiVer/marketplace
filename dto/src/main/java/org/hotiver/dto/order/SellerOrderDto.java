package org.hotiver.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@AllArgsConstructor
@Getter
@Setter
public class SellerOrderDto {
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Date orderDate;
    private Date deliveryDate;
    private String orderStatus;
    private Double totalPrice;
    private String deliveryAddress;
    private String deliveryCity;
    private String recipientName;
    private String recipientPhone;
}
