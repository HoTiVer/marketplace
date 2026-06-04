package org.hotiver.app.service.auth;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.common.Utils.RedisKeyUtils;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.dto.user.UserInfoDto;
import org.hotiver.repo.core.RoleRepo;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.service.auth.AuthService;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.auth.TokensService;
import org.hotiver.service.factory.UserFactory;
import org.hotiver.service.redis.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RedisService redisService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokensService tokensService;

    @Mock
    private UserFactory userFactory;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role userRole;

    @BeforeEach
    public void init() {
        user = new User();
        //user.setId(1L);
        user.setEmail("user@test.com");
        user.setPassword("password");

        userRole = new Role(1L, RoleType.USER);
        user.setRoles(new ArrayList<>(List.of(userRole)));
        user.setDisplayName("user");
    }

    @Nested
    class Register {
        private RegisterRequest registerRequest;
        private JwtTokensDto jwtTokensDto;

        @BeforeEach
        public void setUp() {
            registerRequest = new RegisterRequest(
                    user.getEmail(),
                    user.getPassword(),
                    user.getDisplayName()
            );

            jwtTokensDto = new JwtTokensDto(
                    "refreshToken",
                    "accessToken"
            );
        }

        @Test
        void shouldRegisterUser() {
            when(userRepo.existsUserByEmail(registerRequest.getEmail()))
                    .thenReturn(false);
            when(roleRepo.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));

            when(passwordEncoder.encode(registerRequest.getPassword()))
                    .thenReturn("password");

            when(userFactory.createNewDefaultUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getDisplayName(),
                    userRole
            )).thenReturn(user);

            when(tokensService.generateJwtTokens(user)).thenReturn(jwtTokensDto);

            AuthResponse result = authService.register(registerRequest);
            assertNotNull(result.accessToken());
            assertNotNull(result.refreshToken());
            assertNotNull(result.accessTokenLifeTime());
            assertNotNull(result.refreshTokenLifeTime());

            verify(userRepo).save(user);
            verify(tokensService).saveJwtRefreshTokenInRedis(jwtTokensDto, user);
        }

        @Test
        void shouldThrowException_whenUserExistsByEmail() {
            when(userRepo.existsUserByEmail(registerRequest.getEmail()))
                    .thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class,
                    () -> authService.register(registerRequest)
            );
        }

        @Test
        void shouldThrowException_whenUserRoleNotExists() {
            when(userRepo.existsUserByEmail(registerRequest.getEmail()))
                    .thenReturn(false);
            when(roleRepo.findByName(RoleType.USER)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> authService.register(registerRequest)
            );
        }
    }

    @Nested
    class Login {

        private LoginRequest loginRequest;
        private JwtTokensDto jwtTokensDto;

        @BeforeEach
        public void setUp() {
            loginRequest = new LoginRequest(
                    user.getEmail(),
                    user.getPassword()
            );

            jwtTokensDto = new JwtTokensDto(
                    "refreshToken",
                    "accessToken"
            );
        }

        @Test
        void shouldLoginUser() {
            when(userRepo.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                    .thenReturn(true);

            when(tokensService.generateJwtTokens(user)).thenReturn(jwtTokensDto);

            AuthResponse result = authService.login(loginRequest);
            assertNotNull(result.accessToken());
            assertNotNull(result.refreshToken());
            assertNotNull(result.accessTokenLifeTime());
            assertNotNull(result.refreshTokenLifeTime());

            verify(tokensService).saveJwtRefreshTokenInRedis(jwtTokensDto, user);
        }

        @Test
        void shouldThrowException_whenUserNotFoundByEmail() {
            when(userRepo.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> authService.login(loginRequest)
            );
        }

        @Test
        void shouldThrowException_whenUserPasswordIsIncorrect() {
            when(userRepo.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                    .thenReturn(false);

            assertThrows(BadCredentialsException.class,
                    () -> authService.login(loginRequest)
            );
        }
    }

    @Nested
    class RefreshAccessToken {

        String refreshToken = "refreshToken";

        @BeforeEach
        public void setUp() {
            user.setId(1L);
        }

        @Test
        void shouldRefreshAccessToken() {
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);

            when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);

            when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());

            when(userRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(user));

            String redisKey = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());

            when(redisService.hasKey(redisKey)).thenReturn(true);

            when(redisService.getValue(redisKey)).thenReturn(refreshToken);

            when(jwtService.generateAccessToken(any(SecurityUser.class)))
                    .thenReturn("accessToken");

            AuthResponse result = authService.refresh(refreshToken);
            assertNotNull(result.accessToken());
            assertNull(result.refreshToken());
            assertNotNull(result.accessTokenLifeTime());
            assertNull(result.refreshTokenLifeTime());
        }

        @Test
        void shouldThrowException_whenTokenIsNotValid() {
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);

            assertThrows(NoAuthorizationException.class,
                    () -> authService.refresh(refreshToken)
            );
        }

        @Test
        void shouldThrowException_whenProvidedTokenIsNotRefreshToken() {
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);

            when(jwtService.isRefreshToken(refreshToken)).thenReturn(false);

            assertThrows(NoAuthorizationException.class,
                    () -> authService.refresh(refreshToken)
            );
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);

            when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);

            when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());

            when(userRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> authService.refresh(refreshToken)
            );
        }

        @Test
        void shouldThrowException_whenStoredRefreshTokenIsDifferent() {
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);

            when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);

            when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());

            when(userRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(user));

            String redisKey = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());

            when(redisService.hasKey(redisKey)).thenReturn(true);

            when(redisService.getValue(redisKey))
                    .thenReturn("different-refresh-token");

            assertThrows(NoAuthorizationException.class,
                    () -> authService.refresh(refreshToken)
            );
        }

    }

    @Nested
    class Logout {

        private final String refreshToken = "refreshToken";

        @BeforeEach
        public void setUp() {
            user.setId(1L);
        }

        @Test
        void shouldLogoutUser() {
            when(jwtService.extractUsername(refreshToken))
                    .thenReturn(user.getEmail());

            when(userRepo.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

            String redisKey = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());

            authService.logout(refreshToken);

            verify(redisService).deleteValue(redisKey);
        }

        @Test
        void shouldThrowException_whenUserNotAuthorize() {
            when(jwtService.extractUsername(refreshToken))
                    .thenReturn(user.getEmail());

            when(userRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.empty());

            assertThrows(NoAuthorizationException.class,
                    ()-> authService.logout(refreshToken)
            );
        }
    }

    @Nested
    class GetUserInfoForFrontend {

        @AfterEach
        void clearContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void shouldReturnUserInfoForFrontend() {
            Authentication auth = mock(Authentication.class);

            when(auth.getName()).thenReturn(user.getEmail());

            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);

            SecurityContextHolder.setContext(context);

            when(userRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(user));

            UserInfoDto userInfo = authService.getUserInfoForFrontend();
            assertNotNull(userInfo);
        }

        @Test
        void shouldReturnNull_whenUserNotAuthorized() {
            Authentication auth = mock(Authentication.class);

            when(auth.getName()).thenReturn(null);

            SecurityContext context = mock(SecurityContext.class);

            when(context.getAuthentication()).thenReturn(auth);

            SecurityContextHolder.setContext(context);

            UserInfoDto userInfo = authService.getUserInfoForFrontend();
            assertNull(userInfo);
        }
    }
}
