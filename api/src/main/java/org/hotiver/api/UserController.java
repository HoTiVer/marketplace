package org.hotiver.api;

import org.hotiver.dto.SellerRegisterDto;
import org.hotiver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/cabinet/personal-info")
    public String getPersonalInfo(){
        return null;
    }

    @GetMapping("/cabinet/orders")
    public String getOrdersHistory(){
        return null;
    }

    @GetMapping("/cabinet/wishlist")
    public String getWishList(){
        return null;
    }

    @GetMapping("/cabinet/message")
    public String getMessages(){
        return null;
    }

    @GetMapping("/cabinet/new-seller")
    public String getNewSellerPage(){
        return null;
    }

    @PostMapping("/cabinet/new-seller/register")
    public ResponseEntity sendRegisterRequest(@RequestBody SellerRegisterDto sellerRegisterDto){
        return userService.sendRegisterRequest(sellerRegisterDto);
    }
}
