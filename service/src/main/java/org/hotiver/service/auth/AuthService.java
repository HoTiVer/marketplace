package org.hotiver.service.auth;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.common.Utils.RedisKeyUtils;
import org.hotiver.common.Utils.TimeUtils;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.dto.user.UserInfoDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.factory.UserFactory;
import org.hotiver.service.redis.RedisService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Service
public class AuthService {

    private final Long millisecondsToSaveJwtRefresh;
    private final Long millisecondsToSaveJwtAccess;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokensService tokensService;
    private final UserFactory userFactory;

    public AuthService(JwtService jwtService, RedisService redisService,
                       UserRepo userRepo, RoleRepo roleRepo,
                       PasswordEncoder passwordEncoder, TokensService tokensService,
                       UserFactory userFactory) {
        millisecondsToSaveJwtRefresh = jwtService.getJwtRefreshExpirationMilliseconds();
        millisecondsToSaveJwtAccess =  jwtService.getJwtAccessExpirationMilliseconds();
        this.redisService = redisService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokensService = tokensService;
        this.userFactory = userFactory;
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Register attempt for email={}", registerRequest.getEmail());

        if (userRepo.existsUserByEmail(registerRequest.getEmail())){
            log.warn("User already exists: {}", registerRequest.getEmail());
            throw new EntityAlreadyExistsException("User with email"
                    + registerRequest.getEmail()
                    + "already exists");
        }

        Role userRole = roleRepo.findByName(RoleType.USER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        User user = userFactory.createNewDefaultUser(
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getDisplayName(),
                userRole);

        userRepo.save(user);

        JwtTokensDto jwtTokensDto = tokensService.generateJwtTokens(user);
        tokensService.saveJwtRefreshTokenInRedis(jwtTokensDto, user);

        log.info("User registered successfully: id={}", user.getId());
        return buildAuthResponse(jwtTokensDto);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt: {}", loginRequest.getEmail());
        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password for {}",  loginRequest.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        JwtTokensDto jwtTokensDto = tokensService.generateJwtTokens(user);

        tokensService.saveJwtRefreshTokenInRedis(jwtTokensDto, user);

        log.info("Login success: userId={}", user.getId());
        return buildAuthResponse(jwtTokensDto);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new NoAuthorizationException("Refresh token is invalid");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new NoAuthorizationException("Expected refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String redisKey = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());

        if (!redisService.hasKey(redisKey))
            throw new NoAuthorizationException("Refresh token expired");

        String storedRefresh = redisService.getValue(redisKey);
        if (!storedRefresh.equals(refreshToken)) {
            throw new NoAuthorizationException("Refresh token mismatch");
        }

        SecurityUser securityUser = new SecurityUser(user);

        String accessToken = jwtService.generateAccessToken(securityUser);
        return new AuthResponse(
                accessToken,
                null,
                TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                null
        );
    }

    public void logout(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NoAuthorizationException("You are not authorized"));

        String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
        try {
            redisService.deleteValue(key);
            log.info("User logout: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to delete refresh token for user {}", user.getId(), e);
        }
    }

    public UserInfoDto getUserInfoForFrontend() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UserInfoDto(
                user.getDisplayName(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .toList()
        );
    }

    private AuthResponse buildAuthResponse(JwtTokensDto jwtTokensDto) {
        return new AuthResponse(
                jwtTokensDto.getAccessToken(),
                jwtTokensDto.getRefreshToken(),
                TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                TimeUtils.toSeconds(millisecondsToSaveJwtRefresh)
        );
    }
}
