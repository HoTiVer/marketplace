package org.hotiver.service;

import org.hotiver.common.OrderStatus;
import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.Entity.Order;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.repo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.time.LocalDate;

import java.util.HashSet;
import java.util.Set;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final CartItemRepo cartItemRepo;

    public OrderService(ProductRepo productRepo, UserRepo userRepo,
                        OrderRepo orderRepo, CartItemRepo cartItemRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.cartItemRepo = cartItemRepo;
    }

    @Transactional
    public ResponseEntity<?> createOrder(CreateOrderDto createOrderDto) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        Set<CartItem> userCart = new HashSet<>(user.getCart());

        if (userCart.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        for (CartItem cartItem : userCart) {
            var product = productRepo.findById(cartItem.getProduct().getId()).get();
            Order order = Order.builder()
                    .product(product)
                    .user(user)
                    .seller(product.getSeller())
                    .quantity(cartItem.getQuantity())
                    .orderDate(Date.valueOf(LocalDate.now()))
                    .deliveryDate(null)
                    .status(OrderStatus.CREATED)
                    .totalPrice(product.getPrice() * cartItem.getQuantity())
                    .deliveryCity(createOrderDto.getDeliveryCity())
                    .deliveryAddress(createOrderDto.getDeliveryAddress())
                    .recipientName(createOrderDto.getReceiverName())
                    .recipientPhone(createOrderDto.getReceiverPhone())
                    .build();

            orderRepo.save(order);
            user.getCart().remove(cartItem);
            cartItemRepo.delete(cartItem);

        }

        return ResponseEntity.ok().build();
    }
}
