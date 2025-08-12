package org.hotiver.api;

import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.*;
import org.hotiver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> updateUserContacts(@RequestBody UserContactsDto userContactsDto) {
        return userService.updateUserContacts(userContactsDto);
    }

    @PostMapping("/personal-info/contacts/verify")
    public ResponseEntity<?> verifyChangingUserContacts(@RequestBody CodeVerifyDto codeVerifyDto) {
        return userService.verifyChangingUserContacts(codeVerifyDto);
    }

    @GetMapping("/personal-info/security")
    public ResponseEntity<SecurityInfoDto> getAccountSecurityInfo() {
        return userService.getSecurityInfo();
    }

    @PutMapping("/personal-info/security/2fa")
    public ResponseEntity<?> changeTwoFactorStatus() {
        return userService.changeTwoFactorStatus();
    }

    @PutMapping("/personal-info/security/password")
    public ResponseEntity<?> changeUserPassword(@RequestBody PasswordChangeDto passwordChangeDto) {
        return userService.changeUserPassword(passwordChangeDto);
    }

    @PostMapping("/personal-info/security/password/verify")
    public ResponseEntity<?> verifyChangeUserPassword(@RequestBody PasswordChangeDto passwordChangeDto) {
        return userService.verifyChangeUserPassword(passwordChangeDto);
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
