package org.hotiver.service;

import org.hotiver.common.OrderStatus;
import org.hotiver.domain.Entity.Order;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.order.SellerOrderDto;
import org.hotiver.dto.order.SellerOrdersResponse;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.repo.OrderRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final ChatService chatService;

    public SellerService(SellerRepo sellerRepo, ProductRepo productRepo,
                         OrderRepo orderRepo, ChatService chatService) {
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.chatService = chatService;
    }

    public ResponseEntity<SellerProfileDto> getSellerByUsername(String username) {
        Optional<Seller> opSeller = sellerRepo.findByNickname(username);

        if (opSeller.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        var seller = opSeller.get();

        SellerProfileDto sellerProfileDto = SellerProfileDto.builder()
                .id(seller.getId())
                .displayName(seller.getUser().getDisplayName())
                .nickname(seller.getNickname())
                .rating(seller.getRating())
                .profileDescription(seller.getProfileDescription())
                .build();

        return ResponseEntity.ok().body(sellerProfileDto);
    }

    public ResponseEntity<List<ListProductDto>> getSellerProducts(String username) {
        Optional<Seller> opSeller = sellerRepo.findByNickname(username);

        if (opSeller.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        List<ListProductDto> returnProducts = new ArrayList<>();
        List<Product> sellerProducts = productRepo.findAllVisibleBySellerId(opSeller.get().getId());

        for (var product : sellerProducts) {
            ListProductDto listProductDto = new ListProductDto(
                    product.getId(),
                    product.getName(),
                    product.getPrice());

            returnProducts.add(listProductDto);
        }

        return ResponseEntity.ok().body(returnProducts);
    }

    public ResponseEntity<?> sendMessageToSeller(String username, SendMessageDto message) {
        return chatService.sendMessageToSeller(username, message);
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

        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryDate(Date.valueOf(LocalDate.now()));
        }

        order.setStatus(newStatus);
        orderRepo.save(order);

        return ResponseEntity.ok().build();
    }
}


