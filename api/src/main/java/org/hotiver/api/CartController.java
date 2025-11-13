package org.hotiver.api;

import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<ListProductDto> getUserCart() {
        return cartService.getUserCart();
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addProductToCart(@PathVariable Long productId) {
        return cartService.addProductToCart(productId);
    }

    @DeleteMapping("/{productId}")
    public void deleteProductFromCart(@PathVariable Long productId) {
        cartService.deleteProductFromCart(productId);
    }

}
