package org.hotiver.service;

import org.hotiver.common.OrderStatus;
import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.Entity.Order;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.repo.*;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseDto> createOrder(CreateOrderDto createOrderDto) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        Set<CartItem> userCart = new HashSet<>(user.getCart());

        if (userCart.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        for (CartItem cartItem : userCart) {
            Product product = productRepo.findById(cartItem.getProduct().getId()).get();

            if (product.getSeller().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDto("You cannot buy your own product"));
            }

            Integer quantity = cartItem.getQuantity();
            if (quantity > product.getStockQuantity() && quantity > 0) {
                return ResponseEntity.badRequest().build();
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

        }

        userCart.clear();

        return ResponseEntity.ok().build();
    }
}
