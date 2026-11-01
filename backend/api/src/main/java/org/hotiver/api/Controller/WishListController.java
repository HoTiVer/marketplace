package org.hotiver.api.Controller;

import org.hotiver.dto.product.ListProductDto;
import org.hotiver.service.wishlist.WishListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class WishListController {

    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<ListProductDto>> getPersonalWishList(){
        return ResponseEntity.ok().body(wishListService.getUserWishList());
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<Void> removeProductFromWishList(@PathVariable Long productId){
        wishListService.removeProductFromWishList(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<Void> addProductInWishList(@PathVariable Long productId){
        wishListService.addProductToWishList(productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
