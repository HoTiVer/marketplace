package org.hotiver.api;

import org.hotiver.dto.product.ListProductDto;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.PersonalInfoDto;
import org.hotiver.dto.user.UserContactsDto;
import org.hotiver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RequestMapping("/cabinet")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/personal-info")
    public PersonalInfoDto getPersonalInfo() {
        return userService.getPersonalInfo();
    }

    @GetMapping("/personal-info/contacts")
    public UserContactsDto getUserContacts() {
        return userService.getUserContacts();
    }

    @PutMapping("/personal-info/contacts")
    public ResponseEntity<UserContactsDto> updateUserContacts(@RequestBody UserContactsDto userContactsDto) {
        return userService.updateUserContacts(userContactsDto);
    }

    @PostMapping("/personal-info/contacts/verify")
    public ResponseEntity<?> verifyChangingUserContacts(@RequestBody CodeVerifyDto codeVerifyDto) {
        return userService.verifyChangingUserContacts(codeVerifyDto);
    }

    @GetMapping("/personal-info/security")
    public String getAccountSecurityInfo(){
        return "security info";
    }

    @GetMapping("/orders")
    public String getOrdersHistory() {
        return null;
    }

    @GetMapping("/new-seller")
    public ResponseEntity<?> getNewSellerPage() {
        return userService.getNewSellerInfo();
    }

    @PostMapping("/new-seller/register")
    public ResponseEntity<Map<String, Object>> sendRegisterRequest(
            @RequestBody SellerRegisterDto sellerRegisterDto) {
        return userService.sendRegisterRequest(sellerRegisterDto);
    }
}
