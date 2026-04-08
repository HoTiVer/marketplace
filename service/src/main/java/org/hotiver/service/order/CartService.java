package org.hotiver.service.order;

import org.hotiver.common.Exception.base.ResourceNotFoundException;
import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.keys.CartItemId;
import org.hotiver.dto.cart.CartItemDto;
import org.hotiver.repo.CartItemRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final CartItemRepo cartItemRepo;

    public CartService(UserRepo userRepo, ProductRepo productRepo,
                       CartItemRepo cartItemRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.cartItemRepo = cartItemRepo;
    }

    public List<CartItemDto> getUserCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get();

        return cartItemRepo.findByUserId(user.getId());
    }

    public void addProductToCart(Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Product> product = productRepo.findById(productId);
        if (product.isPresent()) {
            User user = userRepo.findByEmail(email).get();

            CartItemId id = new CartItemId(user.getId(), productId);

            CartItem cartItem = cartItemRepo.findById(id)
                    .orElse(CartItem.builder()
                            .id(id)
                            .user(user)
                            .product(product.get())
                            .quantity(1)
                            .build());

            user.getCart().add(cartItem);
            userRepo.save(user);
        }
        else {
            throw new ResourceNotFoundException("Product not found");
        }
    }

    public void deleteProductFromCart(Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Product> product = productRepo.findById(productId);
        if (product.isPresent()) {
            User user = userRepo.findByEmail(email).get();
            CartItemId id = new CartItemId(user.getId(), productId);
            cartItemRepo.deleteById(id);
        }
    }

    public void updateProductCount(Long productId, Integer count) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        Optional<Product> product = productRepo.findById(productId);
        if (product.isPresent()) {
            User user = userRepo.findByEmail(email).get();
            CartItemId id = new CartItemId(user.getId(), productId);

            CartItem cartItem = cartItemRepo.findById(id).get();
            cartItem.setQuantity(count);

            cartItemRepo.save(cartItem);
        }
    }
}
