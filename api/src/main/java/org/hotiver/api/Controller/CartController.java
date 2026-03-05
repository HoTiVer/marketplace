package org.hotiver.api.Controller;

import org.hotiver.dto.cart.CartItemDto;
import org.hotiver.service.CartService;
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
    public ResponseEntity addProductToCart(@PathVariable Long productId) {
        cartService.addProductToCart(productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity deleteProductFromCart(@PathVariable Long productId) {
        cartService.deleteProductFromCart(productId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}")
    public ResponseEntity updateProductCount(@PathVariable Long productId,
                                   @RequestParam Integer count) {
        cartService.updateProductCount(productId, count);
        return ResponseEntity.ok().build();
    }
}
