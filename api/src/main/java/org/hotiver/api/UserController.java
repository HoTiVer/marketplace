package org.hotiver.api;

import org.hotiver.dto.order.UserOrderDto;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.*;
import org.hotiver.service.OrderService;
import org.hotiver.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RequestMapping("/cabinet")
@RestController
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
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
    public Page<UserOrderDto> getOrdersHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.getUserOrders(page, size);
    }

    @GetMapping("/orders/{orderId}")
    public String getOrdersHistory(
            @PathVariable Integer orderId
    ) {
        return null;
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelUserOrder(orderId);
    }

    @PostMapping("/new-seller/register")
    public ResponseEntity<Map<String, Object>> sendRegisterRequest(
            @RequestBody SellerRegisterDto sellerRegisterDto) {
        return userService.sendRegisterRequest(sellerRegisterDto);
    }
}
