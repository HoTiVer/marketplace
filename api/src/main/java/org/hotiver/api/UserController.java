package org.hotiver.api;

import org.hotiver.dto.SellerRegisterDto;
import org.hotiver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;


@RequestMapping("/cabinet")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/personal-info")
    public String getPersonalInfo(){
        return null;
    }

    @GetMapping("/orders")
    public String getOrdersHistory(){
        return null;
    }

    @GetMapping("/wishlist")
    public String getWishList(){
        return null;
    }

    @GetMapping("/message")
    public String getMessages(){
        return null;
    }

    @GetMapping("/new-seller")
    public String getNewSellerPage(){
        return null;
    }

    @PostMapping("/new-seller/register")
    public ResponseEntity<Map<String, Object>> sendRegisterRequest(@RequestBody SellerRegisterDto sellerRegisterDto){
        return userService.sendRegisterRequest(sellerRegisterDto);
    }
}
