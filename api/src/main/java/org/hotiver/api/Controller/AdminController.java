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

    //TODO add more endpoints for stats
    @GetMapping("/stats")
    public String getStats() {
        return null;
    }

}
