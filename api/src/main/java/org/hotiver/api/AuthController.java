package org.hotiver.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserAuthDto;
import org.hotiver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            description = "Any response contains three parameters: success (true or false), message, and token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"success\": true, \"message\": \"some msg\",  \"token\": \"token\"}"
                                    )
                            ))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserAuthDto userDto){
        return authService.register(userDto);
    }

    @Operation(
            description = "Any response contains three parameters: success (true or false), message, and token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged in successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"success\": true, \"message\": \"some msg\",  \"token\": \"token\"}"
                                    )
                            ))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserAuthDto userAuthDto){
        return authService.login(userAuthDto);
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verifyCode(@RequestBody CodeVerifyDto codeVerifyDto){
        return authService.verifyCode(codeVerifyDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        return authService.refresh(authHeader);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        return authService.logout();
    }
}
