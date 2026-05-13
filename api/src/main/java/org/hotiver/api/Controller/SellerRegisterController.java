package org.hotiver.api.Controller;

import jakarta.validation.Valid;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.service.user.SellerRegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller-requests")
public class SellerRegisterController {

    private final SellerRegisterService sellerRegisterService;

    public SellerRegisterController(SellerRegisterService sellerRegisterService) {
        this.sellerRegisterService = sellerRegisterService;
    }

    @PostMapping
    public ResponseEntity<Void> sendSellerRegisterRequest(
            @Valid @RequestBody SellerRegisterDto sellerRegisterDto) {
        sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<SellerRegisterResponse>> getSellerRegisterRequests() {
        return ResponseEntity.ok().body(sellerRegisterService.getSellerRegisterRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> acceptSellerRegisterRequest(@PathVariable Long id) {
        sellerRegisterService.acceptSellerRegisterRequest(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> declineSellerRegisterRequest(@PathVariable Long id){
        sellerRegisterService.declineSellerRegisterRequest(id);
        return ResponseEntity.ok().build();
    }

}
