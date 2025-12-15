package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.common.OrderStatus;
import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.Entity.Order;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.dto.order.OrderCreatedEvent;
import org.hotiver.dto.order.UserOrderDto;
import org.hotiver.repo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Date;
import java.time.LocalDate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final CartItemRepo cartItemRepo;
    private final RedisOutboxService redisOutboxService;

    public OrderService(ProductRepo productRepo, UserRepo userRepo,
                        OrderRepo orderRepo, CartItemRepo cartItemRepo,
                        RedisOutboxService redisOutboxService) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.cartItemRepo = cartItemRepo;
        this.redisOutboxService = redisOutboxService;
    }

    @Transactional
    public ResponseEntity<ResponseDto> createOrder(CreateOrderDto createOrderDto) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        Set<CartItem> userCart = new HashSet<>(user.getCart());

        if (userCart.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto("You must add products in car before buying"));
        }

        for (CartItem cartItem : userCart) {
            Product product = productRepo.findById(cartItem.getProduct().getId()).get();

            if (product.getSeller().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDto("You cannot buy your own product"));
            }

            Integer quantity = cartItem.getQuantity();
            if (quantity > product.getStockQuantity() && quantity > 0) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("Your order quantity of product "
                                + product.getName() + " exceeds stock"));
            }

            Order order = Order.builder()
                    .product(product)
                    .user(user)
                    .seller(product.getSeller())
                    .quantity(quantity)
                    .orderDate(Date.valueOf(LocalDate.now()))
                    .deliveryDate(null)
                    .status(OrderStatus.CREATED)
                    .totalPrice(product.getPrice() * quantity)
                    .deliveryCity(createOrderDto.getDeliveryCity())
                    .deliveryAddress(createOrderDto.getDeliveryAddress())
                    .recipientName(createOrderDto.getReceiverName())
                    .recipientPhone(createOrderDto.getReceiverPhone())
                    .build();

            product.setSalesCount(product.getSalesCount() + 1);
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepo.save(product);

            orderRepo.save(order);
            user.getCart().remove(cartItem);
            cartItemRepo.delete(cartItem);

            saveOrderInOutbox(order);
        }

        userCart.clear();

        return ResponseEntity.ok().build();
    }

    private void saveOrderInOutbox(Order order) {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .productId(order.getProduct().getId())
                .productName(order.getProduct().getName())
                .categoryId(order.getProduct().getCategory().getId())
                .categoryName(order.getProduct().getCategory().getName())
                .sellerId(order.getSeller().getId())
                .productQuantity(order.getQuantity())
                .totalAmount(order.getTotalPrice())
                .createdAt(order.getOrderDate())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        String json;
        try {
            json = objectMapper.writeValueAsString(orderCreatedEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        redisOutboxService.savePurchaseEvent(json);
    }

    public Page<UserOrderDto> getUserOrders(int page, int size) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        Pageable pageable = PageRequest.of(page, size);
        Page<UserOrderDto> userOrderDto = orderRepo.findUserOrders(user.getId(), pageable);
        return userOrderDto;
    }

    public ResponseEntity<?> cancelUserOrder(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        Order order = orderRepo.findById(orderId).orElse(null);
        OrderStatus newStatus = OrderStatus.CANCELLED;

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        if (order.getUser().getId().equals(user.getId())
                && order.getStatus().canChangeTo(newStatus)) {
                order.setStatus(newStatus);
                orderRepo.save(order);
                return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }
}
