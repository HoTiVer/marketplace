package org.hotiver.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.auth.AuthService;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.auth.OAuth2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final OAuth2Service oAuth2Service;
    @Value("${frontend.url}")
    private String frontendUrl;

    public OAuth2SuccessHandler(JwtService jwtService, UserRepo userRepo,
                                RoleRepo roleRepo, OAuth2Service oAuth2Service) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.oAuth2Service = oAuth2Service;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        AuthResponse authResponse = oAuth2Service.authAsOAuth2User(email);

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

        response.setStatus(HttpServletResponse.SC_OK);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.sendRedirect(frontendUrl);
    }
}
