package org.hotiver.service;

import Utils.EmailUtils;
import Utils.HashUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.auth.AuthDto;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserAuthDto;
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
    public ResponseEntity<AuthDto> register(UserAuthDto userAuthDto) {
        if (userAuthDto == null) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Request body is missing")
                            .build());
        }
        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();
        String displayName = userAuthDto.getDisplayName();

        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Email or Password are required")
                            .build());
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = email;
        }

        if (!EmailUtils.isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Invalid email format")
                            .build());
        }

        if (userRepo.existsUserByEmail(email)){
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("User already exists")
                            .build());
        }

        User user = createNewDefaultUser(email, password, displayName);

        try {
            userRepo.save(user);

            SecurityUser securityUser = new SecurityUser(user);

            JwtTokensDto jwtTokensDto = generateJwtTokens(securityUser);

            String key = generateRedisRefreshTokenKey(user.getId());
            redisService.saveValue(key, jwtTokensDto.getRefreshToken(), timeToSaveJwtRefresh);

            return ResponseEntity.ok()
                    .body(AuthDto.builder()
                            .isSuccess(true)
                            .message("Registered")
                            .refreshToken(jwtTokensDto.getRefreshToken())
                            .accessToken(jwtTokensDto.getAccessToken())
                            .build());

        }
        catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Unexpected error occurred")
                            .build());
        }
    }

    public ResponseEntity<AuthDto> login(UserAuthDto userAuthDto) {
        if (userAuthDto.getEmail() == null || userAuthDto.getEmail().trim().isEmpty() ||
                userAuthDto.getPassword() == null || userAuthDto.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Email and Password are required")
                            .build());
        }

        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();

        if (!EmailUtils.isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Invalid email format")
                            .build());
        }

        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("User not found")
                            .build());
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.builder()
                            .isSuccess(false)
                            .message("Invalid password")
                            .build());
        }

        if (user.get().getIsTwoFactorEnable()){
            sendVerificationCode(email);
            return ResponseEntity.ok()
                    .body(AuthDto.builder()
                            .isSuccess(true)
                            .message("Redirecting")
                            .redirectUrl("/auth/login/verify")
                            .build());
        }

        SecurityUser securityUser = new SecurityUser(user.get());

        String refreshToken = jwtService.generateRefreshToken(securityUser);
        String accessToken = jwtService.generateAccessToken(securityUser);

        String refreshTokenKey = generateRedisRefreshTokenKey(user.get().getId());
        redisService.saveValue(refreshTokenKey,
                refreshToken,
                timeToSaveJwtRefresh);

        return ResponseEntity.ok()
                .body(AuthDto.builder()
                        .isSuccess(true)
                        .message("Logged in successfully")
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build());
    }

    @Transactional
    public ResponseEntity<AuthDto> verifyCode(CodeVerifyDto codeVerifyDto) {
        String key = generateRedisTwoFactorKey(codeVerifyDto.getEmail());
        String storedCode = redisService.getValue(key);

        if (storedCode == null) {
            return ResponseEntity.badRequest().build();
        }

        if (storedCode.equals(codeVerifyDto.getCode())) {
            redisService.deleteValue(key);

            var opUser = userRepo.findByEmail(codeVerifyDto.getEmail());
            if (opUser.isEmpty())
                return ResponseEntity.badRequest().build();

            SecurityUser securityUser = new SecurityUser(opUser.get());
            String refreshToken = jwtService.generateRefreshToken(securityUser);
            String accessToken = jwtService.generateAccessToken(securityUser);

            String refreshTokenKey = generateRedisRefreshTokenKey(opUser.get().getId());

            redisService.saveValue(refreshTokenKey, refreshToken,
                    TimeUnit.MILLISECONDS.toMinutes(timeToSaveJwtRefresh));

            return ResponseEntity.ok()
                    .body(AuthDto.builder()
                            .isSuccess(true)
                            .message("Logged in successfully")
                            .refreshToken(refreshToken)
                            .accessToken(accessToken)
                            .build());
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
                .getValue(generateRedisRefreshTokenKey(user.get().getId()));
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

        String key = generateRedisRefreshTokenKey(user.getId());
        redisService.deleteValue(key);

        return ResponseEntity.ok().build();
    }

    @Transactional
    public JwtTokensDto authAsOAuth2User(String email) {
        Optional<User> opUser = userRepo.findByEmail(email);
        User user;
        if (opUser.isEmpty()){
            String password = generate13SymbolsPassword();
            var response = register(new UserAuthDto(email, password));

            return new JwtTokensDto(response.getBody().getRefreshToken(),
                    response.getBody().getAccessToken());
        }
        else {
            user = opUser.get();
        }

        SecurityUser securityUser = new SecurityUser(user);
        var tokens = generateJwtTokens(securityUser);

        String key = generateRedisRefreshTokenKey(user.getId());
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
        try {
            String json = mapper.writeValueAsString(dto);
            redisService.saveValue(redisKey, json, 10);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

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

    private JwtTokensDto generateJwtTokens(SecurityUser securityUser) {
        String refreshToken = jwtService.generateRefreshToken(securityUser);
        String accessToken = jwtService.generateAccessToken(securityUser);
        return new JwtTokensDto(refreshToken, accessToken);
    }

    private void sendVerificationCode(String email) {
        String code = generate6DigitCode();
        String key = generateRedisTwoFactorKey(email);
        redisService.saveValue(key, code, 10);
        emailService.send(email, "Verify Code", "Your Code: " + code);
    }

    private String generateRedisRefreshTokenKey(Long userId) {
        return "refresh:" + HashUtils.hashKeySha256(userId.toString());
    }

    private String generateRedisTwoFactorKey(String email) {
        return "2fa:" + HashUtils.hashKeySha256(email);
    }

    private String generate6DigitCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private String generate13SymbolsPassword() {
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lower = "abcdefghijklmnopqrstuvwxyz";
        final String digits = "0123456789";
        final String special = "!@#$%^&*()-_=+[]{}";
        final String allChars = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(upper.charAt(random.nextInt(upper.length())));
        passwordChars.add(lower.charAt(random.nextInt(lower.length())));
        passwordChars.add(digits.charAt(random.nextInt(digits.length())));
        passwordChars.add(special.charAt(random.nextInt(special.length())));

        for (int i = passwordChars.size(); i < 13; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
