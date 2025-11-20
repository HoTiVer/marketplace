package org.hotiver.api;

import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.order.SellerOrderDto;
import org.hotiver.dto.order.SellerOrdersResponse;
import org.hotiver.dto.order.UpdateStatusDto;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.service.SellerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/seller/message/{username}")
    public ResponseEntity<?> sendMessageToSeller(@PathVariable String username,
                                                 @RequestBody SendMessageDto message){
        return sellerService.sendMessageToSeller(username, message);
    }

    @PreAuthorize("isAuthenticated() and hasRole('SELLER')")
    @GetMapping("/seller/manage-orders")
    public SellerOrdersResponse getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return sellerService.getSellerOrders(page, size);
    }

    @PreAuthorize("isAuthenticated() and hasRole('SELLER')")
    @PatchMapping("/seller/manage-orders/{orderId}")
    public ResponseEntity<?> changeOrderStatus(@PathVariable Long orderId,
                                               @RequestBody UpdateStatusDto updateStatusDto) {

        return sellerService.changeOrderStatus(orderId, updateStatusDto.getStatus());
    }
}
