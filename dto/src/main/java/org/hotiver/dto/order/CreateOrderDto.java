package org.hotiver.dto.order;

import lombok.Getter;
import lombok.Setter;
import org.hotiver.dto.product.ListProductDto;

import java.util.Map;

@Getter
@Setter
public class CreateOrderDto {
    //private Map<Long, Integer> products;

    private String deliveryAddress;
    private String deliveryCity;
    private String receiverName;
    private String receiverPhone;
}
