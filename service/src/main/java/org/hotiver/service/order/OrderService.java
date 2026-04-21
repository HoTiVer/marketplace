package org.hotiver.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.OrderStatus;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.common.Exception.base.ResourceNotFoundException;
import org.hotiver.common.Exception.order.CannotBuyOwnProductException;
import org.hotiver.domain.Entity.*;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.order.*;
import org.hotiver.repo.*;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.redis.RedisOutboxService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final CartItemRepo cartItemRepo;
    private final SellerRepo sellerRepo;
    private final RedisOutboxService redisOutboxService;
    private final CurrentUserService currentUserService;

    public OrderService(ProductRepo productRepo, UserRepo userRepo,
                        OrderRepo orderRepo, CartItemRepo cartItemRepo,
                        SellerRepo sellerRepo, RedisOutboxService redisOutboxService,
                        CurrentUserService currentUserService) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.cartItemRepo = cartItemRepo;
        this.sellerRepo = sellerRepo;
        this.redisOutboxService = redisOutboxService;
        this.currentUserService = currentUserService;
    }


    @Transactional
    public void createOrder(CreateOrderDto createOrderDto) {
        User user = currentUserService.getCurrentUser();

        if (user.getCart().isEmpty()) {
            throw new EntityNotFoundException("Cart is empty");
        }

        for (CartItem cartItem : new HashSet<>(user.getCart())) {
            Product product = cartItem.getProduct();

            validateBuyer(product, user.getId());

            Order order = createOrder(product, createOrderDto, user, cartItem.getQuantity());

            product.sell(cartItem.getQuantity());

            productRepo.save(product);
            orderRepo.save(order);
            user.getCart().remove(cartItem);
            cartItemRepo.delete(cartItem);
        }
    }

    private void validateBuyer(Product product, Long buyerId) {
        if (product.getSeller().getId().equals(buyerId)) {
            throw new CannotBuyOwnProductException("You cannot buy your own product");
        }
    }

    private Order createOrder(Product product, CreateOrderDto createOrderDto,
                              User user, Integer quantity) {
        return Order.builder()
                .product(product)
                .user(user)
                .seller(product.getSeller())
                .quantity(quantity)
                .orderDate(Date.valueOf(LocalDate.now()))
                .deliveryDate(null)
                .status(OrderStatus.CREATED)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .deliveryCity(createOrderDto.deliveryCity())
                .deliveryAddress(createOrderDto.deliveryAddress())
                .recipientName(createOrderDto.receiverName())
                .recipientPhone(createOrderDto.receiverPhone())
                .build();
    }

    private void saveOrderInOutbox(Order order) {
        if (order.getProduct().getId() == 0L) {
            return;
        }

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
        SecurityUser user = currentUserService.getUserPrincipal();

        Pageable pageable = PageRequest.of(page, size);
        return orderRepo.findUserOrders(user.getId(), pageable);
    }

    public void cancelUserOrder(Long orderId) {
        SecurityUser user = currentUserService.getUserPrincipal();

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        OrderStatus newStatus = OrderStatus.CANCELLED;

        if (order.getUser().getId().equals(user.getId())
                && order.getStatus().canChangeTo(newStatus)) {
                order.setStatus(newStatus);
                orderRepo.save(order);

                Product product = productRepo.findById(order.getProduct().getId())
                                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                product.getBack(order.getQuantity());

                productRepo.save(product);
        }
        else {
            throw new ForbiddenOperationException("You cannot cancel status of this order");
        }
    }

    public SellerOrdersResponse getSellerOrders(int page, int size) {
        SecurityUser user = currentUserService.getUserPrincipal();

        Seller seller = sellerRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Seller not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<SellerOrderDto> sellerOrderDto = orderRepo
                .findSellerOrders(seller.getId(), pageable);

        List<OrderStatus> orderStatuses = List.of(OrderStatus.values());

        return new SellerOrdersResponse(sellerOrderDto, orderStatuses);
    }

    public void changeOrderStatus(Long orderId, String status) {
        SecurityUser user = currentUserService.getUserPrincipal();

        OrderStatus newStatus = parseOrderStatus(status);
        Seller seller = sellerRepo.findByEmail(user.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Seller not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        validateSellerOwnerShip(order, seller.getId());

        if (!order.getStatus().canChangeTo(newStatus)) {
            throw new InvalidStateException("Order status can't be changed");
        }

        applyStatusChange(order, newStatus);

        order.setStatus(newStatus);
        orderRepo.save(order);
    }

    private void applyStatusChange(Order order, OrderStatus newStatus) {
        switch (newStatus) {
            case CANCELLED, RETURNED -> restoreStock(order);
            case DELIVERED -> setDeliveryDate(order);
            case COMPLETED -> saveOrderInOutbox(order);
        }
    }

    private void restoreStock(Order order) {
        Product product = order.getProduct();

        product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
        productRepo.save(product);
    }

    private void setDeliveryDate(Order order) {
        order.setDeliveryDate(Date.valueOf(LocalDate.now()));
    }

    private void validateSellerOwnerShip(Order order, Long sellerId) {
        if (!order.getSeller().getId().equals(sellerId)) {
            throw new ForbiddenOperationException("You are not allowed to change the order status");
        }
    }

    private OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Order status not found");
        }
    }
}
