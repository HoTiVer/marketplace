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

import java.util.*;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public AuthService(UserRepo userRepo, RoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    public ResponseEntity<Map<String, Object>> register(UserDto userDto) {
        String email = userDto.getEmail();

        if (userRepo.existsUserByEmail(userDto.getEmail())){
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User already exists"));
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Optional<Role> role = roleRepo.findById(1L);

        User user = User.builder()
                .email(userDto.getEmail())
                .password(encoder.encode(userDto.getPassword()))
                .balance(0.0)
                .roles(List.of(role.get()))
                .build();

        userRepo.save(user);

        return ResponseEntity.ok(Map.of("success", true,
                "message", "Registered"));
    }

    public ResponseEntity<Map<String, Object>> login(UserDto userDto) {
        User user = userRepo.findByEmail(userDto.getEmail());

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User not found."));
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Wrong password."));
        }


        return ResponseEntity.ok(Map.of("success", true,
                "message", "Logged in"));
    }
}
