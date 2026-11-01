package org.hotiver.api.Controller;

import org.hotiver.dto.cart.CartItemDto;
import org.hotiver.service.order.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItemDto> getUserCart() {
        return cartService.getUserCart();
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addProductToCart(@PathVariable Long productId) {
        cartService.addProductToCart(productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductFromCart(@PathVariable Long productId) {
        cartService.deleteProductFromCart(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateProductCount(@PathVariable Long productId,
                                   @RequestParam Integer count) {
        cartService.updateProductCount(productId, count);
        return ResponseEntity.noContent().build();
    }
}
