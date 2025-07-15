package org.hotiver.api;

import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/request/seller-register")
    public List<SellerRegister> getSellerRegisterRequests(){
        return adminService.getSellerRegisterRequests();
    }

    @PostMapping("/request/seller-register/{id}")
    public void acceptSellerRegisterRequest(@PathVariable String id){
        adminService.acceptSellerRegisterRequest(id);
    }

    @DeleteMapping("/request/seller-register/{id}")
    public void declineSellerRegisterRequest(@PathVariable String id){
        adminService.declineSellerRegisterRequest(id);
    }

}
