package org.hotiver.api.Controller;

import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.dto.order.SellerOrdersResponse;
import org.hotiver.dto.order.UpdateStatusDto;
import org.hotiver.dto.order.UserOrderDto;
import org.hotiver.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createOrder(@RequestBody CreateOrderDto createOrderDto) {
        return orderService.createOrder(createOrderDto);
    }

    @PreAuthorize("isAuthenticated() and hasRole('SELLER')")
    @GetMapping("/seller/manage-orders")
    public SellerOrdersResponse getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.getSellerOrders(page, size);
    }

    @PreAuthorize("isAuthenticated() and hasRole('SELLER')")
    @PatchMapping("/seller/manage-orders/{orderId}")
    public ResponseEntity<?> changeOrderStatus(@PathVariable Long orderId,
                                               @RequestBody UpdateStatusDto updateStatusDto) {

        return orderService.changeOrderStatus(orderId, updateStatusDto.getStatus());
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelUserOrder(orderId);
    }

    @GetMapping("/orders")
    public Page<UserOrderDto> getOrdersHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.getUserOrders(page, size);
    }
}
