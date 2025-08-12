package org.hotiver.service;

import Utils.EmailUtils;
import Utils.HashUtils;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserAuthDto;
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

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class AuthService {

    private final JwtService jwtService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public AuthService(JwtService jwtService, EmailService emailService,
                       RedisService redisService, UserRepo userRepo,
                       RoleRepo roleRepo) {
        this.emailService = emailService;
        this.redisService = redisService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> register(UserAuthDto userAuthDto) {
        if (userAuthDto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Request body is missing"));
        }
        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();
        String displayName = userAuthDto.getDisplayName();

        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Email or Password are required"));
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = email;
        }

        if (!EmailUtils.isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid email format"));
        }

        if (userRepo.existsUserByEmail(email)){

            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User already exists"));
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Role role = roleRepo.findById(1L).orElseThrow();

        User user = User.builder()
                .email(email)
                .password(encoder.encode(password))
                .balance(0.0)
                .roles(List.of(role))
                .registerDate(Date.valueOf(LocalDate.now()))
                .displayName(displayName)
                .isTwoFactorEnable(false)
                .build();

        String refreshToken;
        String accessToken;
        try {
            userRepo.save(user);

            SecurityUser securityUser = new SecurityUser(user);

            refreshToken = jwtService.generateRefreshToken(securityUser);
            accessToken = jwtService.generateAccessToken(securityUser);

            String key = "refresh:" + HashUtils.hashKeySha256(user.getId().toString());
            Long timeToSave = jwtService.getJwtRefreshExpiration();
            redisService.saveValue(key, refreshToken, TimeUnit.MILLISECONDS.toMinutes(timeToSave));

            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Registered",
                    "refreshToken", refreshToken,
                    "accessToken", accessToken));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Unexpected error occurred"));
        }
    }

    public ResponseEntity<Map<String, Object>> login(UserAuthDto userAuthDto) {
        if (userAuthDto.getEmail() == null || userAuthDto.getEmail().trim().isEmpty() ||
                userAuthDto.getPassword() == null || userAuthDto.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Email and Password are required"));
        }

        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();

        if (!EmailUtils.isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid email format"));
        }

        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User not found"));
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Invalid password"));
        }

        if (user.get().getIsTwoFactorEnable()){
            sendVerificationCode(email);
            return ResponseEntity.ok(Map.of(
                    "redirect", "/login/verify",
                    "method", "POST"));
        }

        SecurityUser securityUser = new SecurityUser(user.get());

        String refreshToken = jwtService.generateRefreshToken(securityUser);
        String accessToken = jwtService.generateAccessToken(securityUser);

        String refreshTokenKey = "refresh:" + HashUtils.hashKeySha256(user.get().getId().toString());
        Long timeToSave = jwtService.getJwtRefreshExpiration();
        redisService.saveValue(refreshTokenKey,
                refreshToken,
                TimeUnit.MILLISECONDS.toMinutes(timeToSave));

        return ResponseEntity.ok(Map.of("success", true,
                "message", "Logged in successfully",
                "refreshToken", refreshToken,
                "accessToken", accessToken));
    }

    public ResponseEntity<?> verifyCode(CodeVerifyDto codeVerifyDto) {
        String key = "2fa:" + HashUtils.hashKeySha256(codeVerifyDto.getEmail());
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

            String refreshTokenKey = "refresh:" + HashUtils.hashKeySha256(opUser.get().getId().toString());
            Long timeToSave = jwtService.getJwtRefreshExpiration();
            redisService.saveValue(refreshTokenKey, refreshToken,
                    TimeUnit.MILLISECONDS.toMinutes(timeToSave));

            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Logged in successfully",
                    "refreshToken", refreshToken,
                    "accessToken", accessToken));
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
                .getValue("refresh:" + HashUtils.hashKeySha256(user.get().getId().toString()));
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

        String key = "refresh:" + HashUtils.hashKeySha256(user.getId().toString());
        redisService.deleteValue(key);

        return ResponseEntity.ok().build();
    }

    private void sendVerificationCode(String email){
        String code = generateRandom6DigitCode();
        String key = "2fa:" + HashUtils.hashKeySha256(email);
        redisService.saveValue(key, code, 10);
        emailService.send(email, "Verify Code", "Your Code: " + code);
    }

    private String generateRandom6DigitCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

}
