package org.hotiver.api.Controller;

import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.service.admin.AdminService;
import org.hotiver.service.user.SellerRegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final SellerRegisterService sellerRegisterService;

    public AdminController(AdminService adminService,  SellerRegisterService sellerRegisterService) {
        this.adminService = adminService;
        this.sellerRegisterService = sellerRegisterService;
    }

    @GetMapping("/request/seller-register")
    public ResponseEntity<List<SellerRegisterResponse>> getSellerRegisterRequests() {
        return ResponseEntity.ok().body(sellerRegisterService.getSellerRegisterRequests());
    }

    @PostMapping("/request/seller-register/{id}")
    public ResponseEntity<Void> acceptSellerRegisterRequest(@PathVariable Long id) {
        sellerRegisterService.acceptSellerRegisterRequest(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/request/seller-register/{id}")
    public ResponseEntity<Void> declineSellerRegisterRequest(@PathVariable Long id){
        sellerRegisterService.declineSellerRegisterRequest(id);
        return ResponseEntity.ok().build();
    }

    //TODO add more endpoints for stats
    @GetMapping("/stats")
    public String getStats() {
        return null;
    }

}
