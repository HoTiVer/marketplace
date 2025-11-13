package org.hotiver.service;

import org.hotiver.domain.Entity.Product;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.repo.OrderRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final OrderRepo orderRepo;

    public OrderService(ProductRepo productRepo, UserRepo userRepo,
                        SellerRepo sellerRepo, OrderRepo orderRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.orderRepo = orderRepo;
    }

    public String createOrder(CreateOrderDto createOrderDto) {
        String deliveryAddress;
        String deliveryCity;
        String receiverName;
        String receiverPhone;

        List<Product> products = new ArrayList<>();


        return "order created";
    }
}
