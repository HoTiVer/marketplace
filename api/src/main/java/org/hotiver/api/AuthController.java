package org.hotiver.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.hotiver.dto.user.UserAuthDto;
import org.hotiver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
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
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserAuthDto userAuthDto){
        return authService.login(userAuthDto);
    }
}
