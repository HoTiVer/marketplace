package org.hotiver.service.order;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.keys.CartItemId;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.cart.CartItemDto;
import org.hotiver.repo.CartItemRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.product.ProductImageService;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CartService {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final CartItemRepo cartItemRepo;
    private final CurrentUserService currentUserService;
    private final ProductImageService productImageService;

    public CartService(UserRepo userRepo, ProductRepo productRepo,
                       CartItemRepo cartItemRepo, CurrentUserService currentUserService,
                       ProductImageService productImageService) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.cartItemRepo = cartItemRepo;
        this.currentUserService = currentUserService;
        this.productImageService = productImageService;
    }

    public List<CartItemDto> getUserCart() {
        SecurityUser user = currentUserService.getUserPrincipal();

        List<CartItemDto> userCart = cartItemRepo.findByUserId(user.getId());

        userCart.forEach(cartItemDto -> {
           cartItemDto.setMainImageUrl(
                   productImageService.getImageHostUrl(cartItemDto.getMainImageUrl()));
        });

        return userCart;
    }

    public void addProductToCart(Long productId) {
        User user = currentUserService.getCurrentUser();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItemId id = new CartItemId(user.getId(), productId);

        CartItem cartItem = cartItemRepo.findById(id)
                .orElse(CartItem.builder()
                        .id(id)
                        .user(user)
                        .product(product)
                        .quantity(1)
                        .build());

        user.getCart().add(cartItem);
        userRepo.save(user);
    }

    public void deleteProductFromCart(Long productId) {
        SecurityUser user = currentUserService.getUserPrincipal();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItemId id = new CartItemId(user.getId(), product.getId());
        cartItemRepo.deleteById(id);
    }

    public void updateProductCount(Long productId, Integer count) {
        SecurityUser user = currentUserService.getUserPrincipal();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItemId id = new CartItemId(user.getId(), product.getId());

        CartItem cartItem = cartItemRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        cartItem.setQuantity(count);

        cartItemRepo.save(cartItem);
    }
}
