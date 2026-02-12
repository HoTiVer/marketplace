package org.hotiver.service;

import org.hotiver.common.Utils.HashUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.common.Utils.PasswordUtils;
import org.hotiver.common.Utils.RedisKeyUtils;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.LoginRequest;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserInfoDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class AuthService {

    private final Long timeToSaveJwtRefresh;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final ObjectMapper mapper;

    public AuthService(JwtService jwtService, EmailService emailService,
                       RedisService redisService, UserRepo userRepo,
                       RoleRepo roleRepo, ObjectMapper mapper) {
        timeToSaveJwtRefresh = TimeUnit.MILLISECONDS
                .toMinutes(jwtService.getJwtRefreshExpiration());
        this.emailService = emailService;
        this.redisService = redisService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.mapper = mapper;
    }

    @Transactional
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        if (userRepo.existsUserByEmail(registerRequest.getEmail())){
            return ResponseEntity.badRequest()
                    .build();
        }

        User user = createNewDefaultUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getDisplayName());

        try {
            userRepo.save(user);

            JwtTokensDto jwtTokensDto = generateJwtTokens(user);

            String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
            redisService.saveValue(key, jwtTokensDto.getRefreshToken(), timeToSaveJwtRefresh);

            return ResponseEntity.ok()
                    .body(AuthResponse.builder()
                            .refreshToken(jwtTokensDto.getRefreshToken())
                            .accessToken(jwtTokensDto.getAccessToken())
                            .build());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest()
                    .build();
        }
    }

    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        Optional<User> user = userRepo.findByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.badRequest()
                    .build();
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            return ResponseEntity.badRequest()
                    .build();
        }

        if (user.get().getIsTwoFactorEnable()){
            sendVerificationCode(loginRequest.getEmail());
            return ResponseEntity.ok()
                    .build();
        }

        JwtTokensDto jwtTokensDto = generateJwtTokens(user.get());

        String refreshTokenKey = RedisKeyUtils.generateRedisRefreshTokenKey(user.get().getId());
        redisService.saveValue(refreshTokenKey,
                jwtTokensDto.getRefreshToken(),
                timeToSaveJwtRefresh);

        return ResponseEntity.ok()
                .body(AuthResponse.builder()
                        .accessToken(jwtTokensDto.getAccessToken())
                        .refreshToken(jwtTokensDto.getRefreshToken())
                        .build());
    }

    @Transactional
    public ResponseEntity<AuthResponse> verifyCode(CodeVerifyDto codeVerifyDto) {
        String key = RedisKeyUtils.generateRedisTwoFactorKey(codeVerifyDto.getEmail());
        String storedCode = redisService.getValue(key);

        if (storedCode == null) {
            return ResponseEntity.badRequest().build();
        }

        if (storedCode.equals(codeVerifyDto.getCode())) {
            redisService.deleteValue(key);

            var opUser = userRepo.findByEmail(codeVerifyDto.getEmail());
            if (opUser.isEmpty())
                return ResponseEntity.badRequest().build();


            JwtTokensDto jwtTokensDto = generateJwtTokens(opUser.get());

            String refreshTokenKey = RedisKeyUtils.generateRedisRefreshTokenKey(opUser.get().getId());
            redisService.saveValue(refreshTokenKey, jwtTokensDto.getRefreshToken(),
                    TimeUnit.MILLISECONDS.toMinutes(timeToSaveJwtRefresh));

            return ResponseEntity.ok()
                    .build();
        }

        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> refresh(String authHeader) {
        String refreshToken = authHeader.replace("Bearer ", "");

        if (!jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expected refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        Optional<User> user = userRepo.findByEmail(username);

        if (user.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String storedRefresh = redisService
                .getValue(RedisKeyUtils.generateRedisRefreshTokenKey(user.get().getId()));
        if (storedRefresh == null || !storedRefresh.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Message","Refresh token mismatch"));
        }

        SecurityUser securityUser = new SecurityUser(user.get());

        String newAccessToken = jwtService.generateAccessToken(securityUser);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).orElseThrow();

        String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
        redisService.deleteValue(key);

        return ResponseEntity.ok().build();
    }

    @Transactional
    public JwtTokensDto authAsOAuth2User(String email) {
        Optional<User> opUser = userRepo.findByEmail(email);
        User user;
        if (opUser.isEmpty()){
            String password = PasswordUtils.generatePassword(13);
            var response = register(new RegisterRequest(email, password, email));

            return new JwtTokensDto(response.getBody().getRefreshToken(),
                    response.getBody().getAccessToken());
        }
        else {
            user = opUser.get();
        }

        var tokens = generateJwtTokens(user);

        String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
        redisService.saveValue(key, tokens.getRefreshToken(), timeToSaveJwtRefresh);

        return tokens;
    }

    public ResponseEntity<UserInfoDto> getUserInfoForFrontend() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        String redisKey = "authData:" + user.getId();


        if (redisService.hasKey(redisKey)) {
            try {
                String cached = redisService.getValue(redisKey);
                UserInfoDto cachedDto = mapper.readValue(cached, UserInfoDto.class);
                return ResponseEntity.ok(cachedDto);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        UserInfoDto dto = new UserInfoDto();
        dto.setRoles(
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .toList()
        );
//        try {
//            String json = mapper.writeValueAsString(dto);
//            redisService.saveValue(redisKey, json, 10);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        return ResponseEntity.ok(dto);
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
