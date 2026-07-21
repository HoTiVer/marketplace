package org.hotiver.app.Controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.hotiver.api.Controller.AuthController;
import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.validation.RegisterRequest.EmailUniqueChecker;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.service.auth.AuthService;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.user.UserPasswordService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    private UserPasswordService userPasswordService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private EmailUniqueChecker emailUniqueChecker;

    private static AuthResponse authResponse;

    @BeforeAll
    public static void setup() {
        authResponse = new AuthResponse(
                "accessToken",
                "refreshToken",
                10000L,
                10000L);
    }

    @Nested
    class Register {
        @Test
        public void shouldReturnTokensInCookies_whenSuccessRegister() throws Exception {
            RegisterRequest registerRequest = new RegisterRequest(
                    "test@example.com",
                    "password",
                    "test");

            given(emailUniqueChecker.isUnique(any())).willReturn(true);

            given(authService.register(any(RegisterRequest.class)))
                    .willReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(cookie().value("accessToken", "accessToken"))
                    .andExpect(cookie().value("refreshToken", "refreshToken"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenEmailAlreadyExists() throws Exception {
            RegisterRequest registerRequest = new RegisterRequest(
                    "test@example.com",
                    "password",
                    "test");

            given(emailUniqueChecker.isUnique(any())).willReturn(false);

            given(authService.register(any(RegisterRequest.class)))
                    .willReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());

            verify(authService, times(0)).register(any(RegisterRequest.class));
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenIncorrectEmail() throws Exception {
            RegisterRequest registerRequest = new RegisterRequest(
                    "test",
                    "password",
                    "test");

            given(emailUniqueChecker.isUnique(any())).willReturn(true);

            given(authService.register(any(RegisterRequest.class)))
                    .willReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());

            verify(authService, times(0)).register(any(RegisterRequest.class));
        }
    }

    @Nested
    class Login {
        @Test
        public void shouldReturnOkAndTokensInCookies_whenSuccessLogin() throws Exception {
            LoginRequest loginRequest = new LoginRequest(
                    "test@example.com",
                    "password");

            given(authService.login(any(LoginRequest.class)))
                    .willReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(cookie().value("accessToken", "accessToken"))
                    .andExpect(cookie().value("refreshToken", "refreshToken"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        public void shouldReturnUnauthorized_whenInvalidPassword() throws Exception {
            LoginRequest loginRequest = new LoginRequest(
                    "test@example.com",
                    "password");

            given(authService.login(any(LoginRequest.class)))
                    .willThrow(new BadCredentialsException("Invalid credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        public void shouldReturnUnauthorized_whenUserNotExists() throws Exception {
            LoginRequest loginRequest = new LoginRequest(
                    "test@example.com",
                    "password");

            given(authService.login(any(LoginRequest.class)))
                    .willThrow(new NoAuthorizationException("Not authorized"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenIncorrectEmailInDto() throws Exception {
            LoginRequest loginRequest = new LoginRequest(
                    "test",
                    "password");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());

            verify(authService, times(0)).login(any(LoginRequest.class));
        }
    }

    @Nested
    class Refresh {
        @Test
        public void shouldRefreshAndReturnAccessToken_whenRefreshTokenIsCorrect() throws Exception {
            String refreshToken = "refreshToken";

            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
            when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);


            when(authService.refresh(any(String.class)))
                    .thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(cookie().value("accessToken", "accessToken"));

            verify(authService, times(1)).refresh(any(String.class));
        }

        @Test
        public void shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
            String refreshToken = "refreshToken";

            when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

            when(authService.refresh(any(String.class)))
                    .thenThrow(new NoAuthorizationException("Refresh token is invalid"));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("Refresh token is invalid"));

            verify(authService, times(1)).refresh(any(String.class));
        }

        @Test
        public void shouldReturnUnauthorized_whenAccessTokenInsteadOfRefreshToken() throws Exception {
            String refreshToken = "accessToken";

            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
            when(jwtService.isRefreshToken(refreshToken)).thenReturn(false);

            when(authService.refresh(any(String.class)))
                    .thenThrow(new NoAuthorizationException("Expected refresh token"));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("Expected refresh token"));

            verify(authService, times(1)).refresh(any(String.class));
        }
    }
}
