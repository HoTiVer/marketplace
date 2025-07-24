package org.hotiver.api;

import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.SellerProductDto;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.service.SellerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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
    public ResponseEntity<List<SellerProductDto>> getSellerProducts(@PathVariable String username){
        return sellerService.getSellerProducts(username);
    }
}
