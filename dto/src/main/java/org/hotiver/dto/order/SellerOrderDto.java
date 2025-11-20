package org.hotiver.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hotiver.common.OrderStatus;

import java.sql.Date;
import java.util.List;

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
