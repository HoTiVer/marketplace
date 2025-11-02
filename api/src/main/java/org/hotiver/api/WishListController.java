package org.hotiver.api;

import org.hotiver.dto.product.ListProductDto;
import org.hotiver.service.WishListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WishListController {

    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<ListProductDto>> getPersonalWishList(){
        return wishListService.getUserWishList();
    }

    @DeleteMapping("/wishlist/{productId}")
    public void removeProductFromWishList(@PathVariable Long productId){
        wishListService.removeProductFromWishList(productId);
    }

    @PostMapping("/wishlist/{productId}")
    public void addProductInWishList(@PathVariable Long productId){
        wishListService.addProductInWishList(productId);
    }
}
