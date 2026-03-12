package org.hotiver.api.Controller;

import jakarta.validation.Valid;
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
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderDto createOrderDto) {
        orderService.createOrder(createOrderDto);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> changeOrderStatus(@PathVariable Long orderId,
                                                  @RequestBody UpdateStatusDto updateStatusDto) {

        orderService.changeOrderStatus(orderId, updateStatusDto.getStatus());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelUserOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public Page<UserOrderDto> getUserOrdersHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.getUserOrders(page, size);
    }
}
