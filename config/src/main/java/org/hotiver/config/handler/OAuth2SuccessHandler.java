package org.hotiver.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.AuthService;
import org.hotiver.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final AuthService authService;

    public OAuth2SuccessHandler(JwtService jwtService, UserRepo userRepo, RoleRepo roleRepo, AuthService authService) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        JwtTokensDto jwtTokensDto = authService.authAsOAuth2User(email);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("""
        {
          "accessToken":"%s",
          "refreshToken":"%s"
        }
        """.formatted(jwtTokensDto.getAccessToken(), jwtTokensDto.getRefreshToken()));
    }
}
