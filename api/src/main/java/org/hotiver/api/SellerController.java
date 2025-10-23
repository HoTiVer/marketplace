package org.hotiver.api;

import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.service.SellerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/seller/{username}")
    public ResponseEntity<SellerProfileDto> getSellerByUsername(@PathVariable String username){
        return sellerService.getSellerByUsername(username);
    }

    @GetMapping("/seller/{username}/products")
    public ResponseEntity<List<ListProductDto>> getSellerProducts(@PathVariable String username){
        return sellerService.getSellerProducts(username);
    }
}
