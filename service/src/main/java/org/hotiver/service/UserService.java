package org.hotiver.service;

import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.SellerRegisterDto;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final SellerRegisterRepo sellerRegisterRepo;
    private final SellerRepo sellerRepo;

    public UserService(UserRepo userRepo, SellerRegisterRepo sellerRegisterRepo,
                       SellerRepo sellerRepo) {
        this.userRepo = userRepo;
        this.sellerRegisterRepo = sellerRegisterRepo;
        this.sellerRepo = sellerRepo;
    }

    public ResponseEntity<Map<String, Object>> sendRegisterRequest(SellerRegisterDto sellerRegisterDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (sellerRepo.existsByNickname(sellerRegisterDto.getRequestedNickname())){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "seller with this nickname exists"));
        }

        String email = authentication.getName();

        Optional<User> optionalUser = userRepo.findByEmail(email);

        Long userId = optionalUser.get().getId();
        if (sellerRepo.existsById(userId)){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "you already a seller"));
        }

        SellerRegister sellerRegister = SellerRegister.builder()
                .userId(userId)
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .profileDescription(sellerRegisterDto.getDescription())
                .build();

        try {
            sellerRegisterRepo.save(sellerRegister);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(Map.of("message", "successfully send request"));
    }
}
