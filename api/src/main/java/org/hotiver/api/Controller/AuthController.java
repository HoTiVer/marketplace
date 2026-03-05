package org.hotiver.api.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RefreshTokenResponse;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserInfoDto;
import org.hotiver.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            description = """
                    Success response should contain two tokens,
                     if some error has happened then you should expect error json""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registered successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"accessToken\": token," +
                                                    " \"refreshToken\": \"token\"}"
                                    )
                            ))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @Operation(
            description = """
                    Success response should contain two tokens,
                     if some error has happened then you should expect error json""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged in successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"accessToken\": token," +
                                                    " \"refreshToken\": \"token\"}"
                                    )
                            ))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok().body(authService.login(loginRequest));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyCode(@RequestBody CodeVerifyDto codeVerifyDto){
        AuthResponse response = authService.verifyCode(codeVerifyDto);
        if (response != null) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok().body(authService.refresh(authHeader));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        authService.logout();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getUserInfoForFrontend() {
        return ResponseEntity.ok().body(authService.getUserInfoForFrontend());
    }
}
