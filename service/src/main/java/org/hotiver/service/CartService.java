package org.hotiver.service;

import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public CartService(UserRepo userRepo, ProductRepo productRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    public List<ListProductDto> getUserCart() {
        var context = SecurityContextHolder.getContext();
        var email =  context.getAuthentication().getName();

        User user = userRepo.findByEmail(email).get();

        List<ListProductDto> cart =  new ArrayList<ListProductDto>();

        for (var product : user.getCart()) {
            ListProductDto productDto = new ListProductDto(product.getId(), product.getName(), product.getPrice());
            cart.add(productDto);
        }
        return cart;
    }

    public ResponseEntity<?> addProductToCart(Long productId) {
        var context = SecurityContextHolder.getContext();
        var email =  context.getAuthentication().getName();

        User user = userRepo.findByEmail(email).get();

        Optional<Product> product = productRepo.findById(productId);

        if (product.isPresent()) {
            user.getCart().add(product.get());
            userRepo.save(user);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    public void deleteProductFromCart(Long productId) {
        var context = SecurityContextHolder.getContext();
        var email =  context.getAuthentication().getName();

        User user = userRepo.findByEmail(email).get();

        Optional<Product> product = productRepo.findById(productId);
        product.ifPresent(value -> user.getCart().remove(value));
        userRepo.save(user);
    }
}
