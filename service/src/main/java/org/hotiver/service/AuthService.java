package org.hotiver.service;

import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserAuthDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required"));
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = email;
        }

        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid email format"));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Password is required"));
        }

        if (userRepo.existsUserByEmail(email)){

            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User already exists"));
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Optional<Role> role = roleRepo.findById(1L);

        User user = User.builder()
                .email(email)
                .password(encoder.encode(password))
                .balance(0.0)
                .roles(List.of(role.get()))
                .registerDate(Date.valueOf(LocalDate.now()))
                .displayName(displayName)
                .isTwoFactorEnable(false)
                .build();

        String token = null;
        try {
            userRepo.save(user);

            SecurityUser securityUser = new SecurityUser(user);
            Map<String, Object> claims =new HashMap<>();
            claims.put("roles", securityUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            token = jwtService.generateToken(claims, securityUser);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Unexpected error occurred"));
        }
        return ResponseEntity.ok(Map.of("success", true,
                "message", "Registered",
                "token", token));
    }

    public ResponseEntity<Map<String, Object>> login(UserAuthDto userAuthDto) {
        if (userAuthDto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Request body is missing"));
        }
        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required"));
        }

        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid email format"));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Password is required"));
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

        String key = "2fa:" + email;
        String storedCode = redisService.getValue(key);
        if (user.get().getIsTwoFactorEnable()){
            sendVerificationCode(email);
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.LOCATION, "/login/verify")
                    .build();
        }

        SecurityUser securityUser = new SecurityUser(user.get());
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = jwtService.generateToken(claims, securityUser);

        return ResponseEntity.ok(Map.of("success", true,
                "message", "Logged in successfully",
                "token", token));
    }

    public ResponseEntity<?> verifyCode(CodeVerifyDto codeVerifyDto) {
        String key = "2fa:" + codeVerifyDto.getEmail();
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
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", securityUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            String token = jwtService.generateToken(claims, securityUser);

            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Logged in successfully",
                    "token", token));
        }

        return ResponseEntity.badRequest().build();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
    }

    private void sendVerificationCode(String email){
        String code = generateRandom6DigitCode();
        redisService.saveValue("2fa:" + email, code, 10);
        emailService.send(email, "Verify Code", "Your Code: " + code);
    }

    private String generateRandom6DigitCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

}
