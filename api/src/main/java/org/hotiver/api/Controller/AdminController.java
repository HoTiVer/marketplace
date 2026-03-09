package org.hotiver.api.Controller;

import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/request/seller-register")
    public ResponseEntity<List<SellerRegister>> getSellerRegisterRequests() {
        return ResponseEntity.ok().body(adminService.getSellerRegisterRequests());
    }

    @PostMapping("/request/seller-register/{id}")
    public ResponseEntity<Void> acceptSellerRegisterRequest(@PathVariable Long id) {
        adminService.acceptSellerRegisterRequest(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/request/seller-register/{id}")
    public ResponseEntity<Void> declineSellerRegisterRequest(@PathVariable Long id){
        adminService.declineSellerRegisterRequest(id);
        return ResponseEntity.ok().build();
    }

    //TODO add more endpoints for stats
    @GetMapping("/stats")
    public String getStats() {
        return null;
    }

}
