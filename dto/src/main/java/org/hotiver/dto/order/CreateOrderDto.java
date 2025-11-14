package org.hotiver.dto.order;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateOrderDto {
    private String deliveryAddress;
    private String deliveryCity;
    private String receiverName;
    private String receiverPhone;
}
