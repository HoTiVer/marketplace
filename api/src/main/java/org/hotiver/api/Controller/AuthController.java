package org.hotiver.api.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.user.UserInfoDto;
import org.hotiver.service.auth.AuthService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        AuthResponse authResponse = authService.register(registerRequest);

        CookieData cookieData = setSuccessAuthCookie(authResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieData.accessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieData.refreshCookie().toString())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);

        CookieData cookieData = setSuccessAuthCookie(authResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieData.accessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieData.refreshCookie().toString())
                .build();
    }

    private CookieData setSuccessAuthCookie(AuthResponse authResponse) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken",
                        authResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(authResponse.accessTokenLifeTime())
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",
                        authResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(authResponse.refreshTokenLifeTime())
                .sameSite("Strict")
                .build();

        return new CookieData(accessCookie, refreshCookie);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request) {
        String refreshToken = getCookie(request, "refreshToken");

        if (refreshToken != null) {

            AuthResponse authResponse = authService.refresh(refreshToken);

            ResponseCookie accessToken = ResponseCookie
                    .from("accessToken", authResponse.accessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(authResponse.accessTokenLifeTime())
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessToken.toString())
                    .build();
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = getCookie(request, "refreshToken");

        if (refreshToken != null)
            authService.logout(refreshToken);

        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getUserInfoForFrontend() {
        return ResponseEntity.ok().body(authService.getUserInfoForFrontend());
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}

record CookieData (
        ResponseCookie accessCookie,
        ResponseCookie refreshCookie
) { }
