package org.hotiver.app.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hotiver.api.Controller.AuthController;
import org.hotiver.common.Exception.NoAuthorizationException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.config.handler.OAuth2FailureHandler;
import org.hotiver.config.handler.OAuth2SuccessHandler;
import org.hotiver.config.security.SecurityConfig;
import org.hotiver.config.service.CustomUserDetailsService;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RefreshTokenResponse;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.validation.RegisterRequest.EmailUniqueChecker;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.AuthService;
import org.hotiver.service.EmailService;
import org.hotiver.service.JwtService;
import org.hotiver.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private EmailUniqueChecker emailUniqueChecker;

    @Test
    public void success_register_test() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password",
                "test");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.ok()
                .body(new AuthResponse(
                "accessToken",
                "refreshToken"));

        when(emailUniqueChecker.isUnique(any())).thenReturn(true);

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    public void email_already_exists_register_test() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password",
                "test");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.status(422)
                .body(new AuthResponse(
                        "accessToken",
                        "refreshToken"));

        when(emailUniqueChecker.isUnique(any())).thenReturn(false);

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().is(422));
    }

    @Test
    public void incorrect_email_register_test() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "test",
                "password",
                "test");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.status(422)
                .body(new AuthResponse(
                        "accessToken",
                        "refreshToken"));

        when(emailUniqueChecker.isUnique(any())).thenReturn(true);

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().is(422));
    }

    @Test
    public void success_login_test() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.status(200)
                .body(new AuthResponse(
                        "accessToken",
                        "refreshToken"));

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accessToken")
                        .value("accessToken"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refreshToken"));
    }

    @Test
    public void invalid_password_login_test() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.badRequest()
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().is(400));
    }

    @Test
    public void user_not_exists_login_test() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.badRequest()
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().is(400));
    }

    @Test
    public void not_correct_email_format_login_test() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                "test",
                "password");

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.status(422)
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().is(422));
    }

    @Test
    public void success_refresh_test() throws Exception {
        String refreshToken = "refreshToken";
        ResponseEntity<RefreshTokenResponse> refreshTokenResponse = ResponseEntity.ok(
                new RefreshTokenResponse("accessToken"));


        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);


        when(authService.refresh(any(String.class)))
                .thenReturn(refreshTokenResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accessToken")
                        .value("accessToken"));
    }

    @Test
    public void invalid_refresh_token_test() throws Exception {
        String refreshToken = "refreshToken";

        when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

        when(authService.refresh(any(String.class)))
                .thenThrow(new NoAuthorizationException("Refresh token is invalid"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andDo(print())
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message")
                        .value("Refresh token is invalid"));
    }

    @Test
    public void access_token_instead_of_refresh_token_test() throws Exception {
        String refreshToken = "accessToken";

        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(false);

        when(authService.refresh(any(String.class)))
                .thenThrow(new NoAuthorizationException("Expected refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andDo(print())
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message")
                        .value("Expected refresh token"));
    }
}
