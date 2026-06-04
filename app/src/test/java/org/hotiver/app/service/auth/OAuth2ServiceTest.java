package org.hotiver.app.service.auth;

import org.hotiver.common.Enum.RoleType;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.repo.core.RoleRepo;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.auth.OAuth2Service;
import org.hotiver.service.auth.TokensService;
import org.hotiver.service.factory.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuth2ServiceTest {

    @Mock
    private JwtService jwtService;

//    @Mock
//    private Long millisecondsToSaveJwtRefresh;
//
//    @Mock
//    private Long millisecondsToSaveJwtAccess;

    @Mock
    private UserRepo userRepo;

    @Mock
    private TokensService tokensService;

    @Mock
    private UserFactory userFactory;

    @Mock
    private RoleRepo roleRepo;

    @InjectMocks
    private OAuth2Service oAuth2Service;

    private String email = "test@test.com";
    private User user;
    private Role userRole;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail(email);
        user.setPassword("password");

        userRole = new Role(1L, RoleType.USER);
    }

    @Nested
    class AuthAsOAuth2User {

        @Test
        void shouldRegisterNewUser() {
            JwtTokensDto jwtTokensDto = new JwtTokensDto(
                    "token",
                    "token"
            );

            when(userRepo.findByEmail(email))
                    .thenReturn(Optional.empty());

            when(roleRepo.findByName(RoleType.USER))
                    .thenReturn(Optional.of(userRole));

            String displayName = email.substring(0, email.indexOf('@'));
            when(userFactory.createNewDefaultUser(
                    email,
                    null,
                    displayName,
                    userRole
            )).thenReturn(user);

            when(tokensService.generateJwtTokens(user))
                    .thenReturn(jwtTokensDto);


            AuthResponse result = oAuth2Service.authAsOAuth2User(email);

            assertNotNull(result.refreshToken());
            assertNotNull(result.accessToken());
            assertNotNull(result.refreshTokenLifeTime());
            assertNotNull(result.accessTokenLifeTime());

            assertEquals(jwtTokensDto.getRefreshToken(), result.refreshToken());
            assertEquals(jwtTokensDto.getAccessToken(), result.accessToken());

            verify(userRepo).save(user);
            verify(tokensService).saveJwtRefreshTokenInRedis(jwtTokensDto, user);
        }

        @Test
        void shouldLoginExistedUser() {
            user.setId(1L);
            JwtTokensDto jwtTokensDto = new JwtTokensDto(
                    "token",
                    "token"
            );

            when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

            when(tokensService.generateJwtTokens(user))
                    .thenReturn(jwtTokensDto);

            AuthResponse result = oAuth2Service.authAsOAuth2User(email);

            assertNotNull(result.refreshToken());
            assertNotNull(result.accessToken());
            assertNotNull(result.refreshTokenLifeTime());
            assertNotNull(result.accessTokenLifeTime());

            assertEquals(jwtTokensDto.getRefreshToken(), result.refreshToken());
            assertEquals(jwtTokensDto.getAccessToken(), result.accessToken());

            verify(userRepo, never()).save(user);
            verify(tokensService).saveJwtRefreshTokenInRedis(jwtTokensDto, user);
        }

    }
}
