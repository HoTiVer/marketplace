package org.hotiver.api.Controller;

import jakarta.validation.Valid;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.*;
import org.hotiver.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cabinet")
public class UserController {

    private final UserService userService;;

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
    public ResponseEntity<RedirectResponse> updateUserContacts(
            @Valid @RequestBody UserContactsDto userContactsDto) {
        userService.updateUserContacts(userContactsDto);
        return ResponseEntity.ok().body(
                new RedirectResponse(
                        "api/v1/cabinet/personal-info/contacts/verify",
                        "POST"
                )
        );
    }

    @PostMapping("/personal-info/contacts/verify")
    public ResponseEntity<AuthResponse> verifyChangingUserContacts(
            @RequestBody CodeVerifyDto codeVerifyDto) {
        AuthResponse authResponse = userService.verifyChangingUserContacts(codeVerifyDto);
        if(authResponse != null) {
            return ResponseEntity.ok().body(authResponse);
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/personal-info/security")
    public ResponseEntity<SecurityInfoDto> getAccountSecurityInfo() {
        return ResponseEntity.ok().body(userService.getSecurityInfo());
    }

    @PostMapping("/new-seller/register")
    public ResponseEntity<Void> sendSellerRegisterRequest(
            @Valid @RequestBody SellerRegisterDto sellerRegisterDto) {
        userService.sendSellerRegisterRequest(sellerRegisterDto);
        return ResponseEntity.ok().build();
    }
}
record RedirectResponse(String redirectUrl, String method) { }
