package org.hotiver.dto.order;

import org.hotiver.common.Enum.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public record SellerOrdersResponse(
        Page<SellerOrderDto> orders,
        List<OrderStatus> statuses
) {}