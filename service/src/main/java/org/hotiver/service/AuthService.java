package org.hotiver.service;

import org.hotiver.common.RoleType;
import org.hotiver.domain.Role;
import org.hotiver.domain.User;
import org.hotiver.dto.UserDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public AuthService(UserRepo userRepo, RoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> register(UserDto userDto) {
        if (userDto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Request body is missing"));
        }
        String email = userDto.getEmail();
        String password = userDto.getPassword();

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
                .build();

        try {
            userRepo.save(user);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Unexpected error occurred"));
        }
        return ResponseEntity.ok(Map.of("success", true,
                "message", "Registered"));
    }

    public ResponseEntity<Map<String, Object>> login(UserDto userDto) {
        if (userDto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Request body is missing"));
        }
        String email = userDto.getEmail();
        String password = userDto.getPassword();

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

        User user = userRepo.findByEmail(email);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User not found."));
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Invalid password"));
        }


        return ResponseEntity.ok(Map.of("success", true,
                "message", "Logged in successfully"));
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(regex);
    }
}
