package org.hotiver.service;

import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.UserAuthDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public AuthService(JwtService jwtService, UserRepo userRepo, RoleRepo roleRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> register(UserAuthDto userAuthDto) {
        if (userAuthDto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Request body is missing",
                            "token", null));
        }
        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required",
                            "token", null));
        }

        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid email format",
                            "token", null));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Password is required",
                            "token", null));
        }

        if (userRepo.existsUserByEmail(email)){
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User already exists",
                            "token", null));
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Optional<Role> role = roleRepo.findById(1L);

        User user = User.builder()
                .email(email)
                .password(encoder.encode(password))
                .balance(0.0)
                .roles(List.of(role.get()))
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
                            "message", "Unexpected error occurred",
                            "token", null));
        }
        return ResponseEntity.ok(Map.of("success", true,
                "message", "Registered",
                "token", token));
    }

    public ResponseEntity<Map<String, Object>> login(UserAuthDto userAuthDto) {
        if (userAuthDto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Request body is missing",
                            "token", null));
        }
        String email = userAuthDto.getEmail();
        String password = userAuthDto.getPassword();

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required",
                            "token", null));
        }

        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid email format",
                            "token", null));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Password is required",
                            "token", null));
        }

        User user = userRepo.findByEmail(email);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User not found",
                            "token", null));
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Invalid password",
                            "token", null));
        }

        SecurityUser securityUser = new SecurityUser(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = jwtService.generateToken(claims, securityUser);

        return ResponseEntity.ok(Map.of("success", true,
                "message", "Logged in successfully",
                "token", token));
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(regex);
    }
}
