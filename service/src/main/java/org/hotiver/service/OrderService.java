package org.hotiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.common.OrderStatus;
import org.hotiver.domain.Entity.*;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.order.*;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final CartItemRepo cartItemRepo;
    private final SellerRepo sellerRepo;
    private final RedisOutboxService redisOutboxService;

    public OrderService(ProductRepo productRepo, UserRepo userRepo,
                        OrderRepo orderRepo, CartItemRepo cartItemRepo,
                        SellerRepo sellerRepo, RedisOutboxService redisOutboxService) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.cartItemRepo = cartItemRepo;
        this.sellerRepo = sellerRepo;
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

            //saveOrderInOutbox(order);
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

                Product product = productRepo.findById(order.getProduct().getId()).orElse(null);
                if (product == null) {
                    return ResponseEntity.badRequest().build();
                }
                product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
                productRepo.save(product);

                return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    public SellerOrdersResponse getSellerOrders(int page, int size) {
        var context = SecurityContextHolder.getContext();
        var email = context.getAuthentication().getName();

        Optional<Seller> opSeller = sellerRepo.findByEmail(email);
        if (opSeller.isEmpty()) {
            throw new RuntimeException("Seller not found");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<SellerOrderDto> sellerOrderDto = orderRepo
                .findSellerOrders(opSeller.get().getId(), pageable);

        List<OrderStatus> orderStatuses = List.of(OrderStatus.values());

        return new SellerOrdersResponse(sellerOrderDto, orderStatuses);
    }

    public ResponseEntity<?> changeOrderStatus(Long orderId, String status) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Seller seller = sellerRepo.findByEmail(email).orElse(null);
        Order order = orderRepo.findById(orderId).orElse(null);

        if (seller == null || order == null) {
            return ResponseEntity.notFound().build();
        }

        if (!order.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!order.getStatus().canChangeTo(newStatus)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED) {
            Product product = productRepo.findById(order.getProduct().getId()).orElse(null);
            if (product == null) {
                return ResponseEntity.badRequest().build();
            }
            product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
            productRepo.save(product);
        }

        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryDate(Date.valueOf(LocalDate.now()));
        }

        if (newStatus == OrderStatus.COMPLETED) {
            saveOrderInOutbox(order);
        }

        order.setStatus(newStatus);
        orderRepo.save(order);

        return ResponseEntity.ok().build();
    }
}
