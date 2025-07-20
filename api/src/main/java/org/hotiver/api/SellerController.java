package org.hotiver.api;

import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.SellerProfileDto;
import org.hotiver.service.SellerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

}
