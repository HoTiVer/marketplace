package org.hotiver.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.common.Exception.auth.InvalidCredentialsException;
import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.common.Utils.PasswordUtils;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class AuthService {

    private final Long millisecondsToSaveJwtRefresh;
    private final Long millisecondsToSaveJwtAccess;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public AuthService(JwtService jwtService, EmailService emailService,
                       RedisService redisService, UserRepo userRepo,
                       RoleRepo roleRepo) {
        millisecondsToSaveJwtRefresh = jwtService.getJwtRefreshExpirationMilliseconds();
        millisecondsToSaveJwtAccess =  jwtService.getJwtAccessExpirationMilliseconds();
        this.emailService = emailService;
        this.redisService = redisService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepo.existsUserByEmail(registerRequest.getEmail())){
            throw new EntityAlreadyExistsException("User with email"
                    + registerRequest.getEmail()
                    + "already exists");
        }

        User user = createNewDefaultUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getDisplayName());


        userRepo.save(user);

        JwtTokensDto jwtTokensDto = generateJwtTokens(user);

        String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
        redisService.saveValue(key, jwtTokensDto.getRefreshToken(),
                TimeUtils.toMinutes(millisecondsToSaveJwtRefresh));

        return new AuthResponse(
                jwtTokensDto.getAccessToken(),
                jwtTokensDto.getRefreshToken(),
                TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                TimeUtils.toSeconds(millisecondsToSaveJwtRefresh)
        );
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Optional<User> user = userRepo.findByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User "
             + loginRequest.getEmail() +  " is not found");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

//        if (user.get().getIsTwoFactorEnable()){
//            sendVerificationCode(loginRequest.getEmail());
//        }

        JwtTokensDto jwtTokensDto = generateJwtTokens(user.get());

        String refreshTokenKey = RedisKeyUtils.generateRedisRefreshTokenKey(user.get().getId());
        redisService.saveValue(refreshTokenKey,
                jwtTokensDto.getRefreshToken(),
                TimeUtils.toMinutes(millisecondsToSaveJwtRefresh));

        return new AuthResponse(
                jwtTokensDto.getAccessToken(),
                jwtTokensDto.getRefreshToken(),
                TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                TimeUtils.toSeconds(millisecondsToSaveJwtRefresh)
        );
    }

//    @Transactional
//    public AuthResponse verifyCode(CodeVerifyDto codeVerifyDto) {
//        String key = RedisKeyUtils.generateRedisTwoFactorKey(codeVerifyDto.getEmail());
//        String storedCode = redisService.getValue(key);
//
//        if (storedCode == null) {
//            return null;
//        }
//
//        if (storedCode.equals(codeVerifyDto.getCode())) {
//            redisService.deleteValue(key);
//
//            var opUser = userRepo.findByEmail(codeVerifyDto.getEmail());
//            if (opUser.isEmpty())
//                return null;
//
//
//            JwtTokensDto jwtTokensDto = generateJwtTokens(opUser.get());
//
//            String refreshTokenKey = RedisKeyUtils.generateRedisRefreshTokenKey(opUser.get().getId());
//            redisService.saveValue(refreshTokenKey, jwtTokensDto.getRefreshToken(),
//                    TimeUnit.MILLISECONDS.toMinutes(timeToSaveJwtRefresh));
//
//            return new AuthResponse(refreshTokenKey, jwtTokensDto.getRefreshToken());
//        }
//
//        return null;
//    }

    public AuthResponse refresh(String refreshToken) {

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new NoAuthorizationException("Refresh token is invalid");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new NoAuthorizationException("Expected refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        Optional<User> user = userRepo.findByEmail(username);

        String storedRefresh = redisService
                .getValue(RedisKeyUtils.generateRedisRefreshTokenKey(user.get().getId()));
        if (!storedRefresh.equals(refreshToken)) {
            throw new NoAuthorizationException("Refresh token mismatch");
        }

        SecurityUser securityUser = new SecurityUser(user.get());

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
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Transactional
    public AuthResponse authAsOAuth2User(String email) {
        Optional<User> opUser = userRepo.findByEmail(email);
        User user;
        if (opUser.isEmpty()){
            String password = PasswordUtils.generatePassword(13);
            String displayName = email.substring(0, email.indexOf('@'));
            AuthResponse response = register(new RegisterRequest(email, password, displayName));

            return new AuthResponse(
                    response.accessToken(),
                    response.refreshToken(),
                    TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                    TimeUtils.toSeconds(millisecondsToSaveJwtRefresh)
            );
        }
        else {
            user = opUser.get();
        }

        JwtTokensDto tokens = generateJwtTokens(user);

        String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
        redisService.saveValue(key, tokens.getRefreshToken(),
                TimeUtils.toMinutes(millisecondsToSaveJwtRefresh));

        return new AuthResponse(
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                TimeUtils.toSeconds(millisecondsToSaveJwtRefresh)
        );
    }

    public UserInfoDto getUserInfoForFrontend() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        UserInfoDto dto = new UserInfoDto(
                user.getDisplayName(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .toList()
        );
        return dto;
    }

    private User createNewDefaultUser(String email, String password, String displayName) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Role role = roleRepo.findById(1L).orElseThrow();

        return User.builder()
                .email(email)
                .password(encoder.encode(password))
                .balance(0.0)
                .roles(List.of(role))
                .registerDate(Date.valueOf(LocalDate.now()))
                .displayName(displayName)
                .isTwoFactorEnable(false)
                .build();
    }

    private JwtTokensDto generateJwtTokens(User user) {
        SecurityUser securityUser = new SecurityUser(user);
        String refreshToken = jwtService.generateRefreshToken(securityUser);
        String accessToken = jwtService.generateAccessToken(securityUser);
        return new JwtTokensDto(refreshToken, accessToken);
    }

    private void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String key = RedisKeyUtils.generateRedisTwoFactorKey(email);
        redisService.saveValue(key, code, 10);
        emailService.send(email, "Verify Code", "Your Code: " + code);
    }
}
