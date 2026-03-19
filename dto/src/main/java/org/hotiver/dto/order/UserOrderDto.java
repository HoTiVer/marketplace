package org.hotiver.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

@AllArgsConstructor
@Getter
@Setter
public class UserOrderDto {
    private Long orderId;
    private Long productId;
    private String productName;
    private String sellerNickname;
    private Integer quantity;
    private Date orderDate;
    private Date deliveryDate;
    private String orderStatus;
    private BigDecimal totalPrice;
    private String deliveryAddress;
}
