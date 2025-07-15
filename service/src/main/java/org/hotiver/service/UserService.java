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

    public ResponseEntity sendRegisterRequest(SellerRegisterDto sellerRegisterDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (sellerRepo.existsByNickname(sellerRegisterDto.getRequestedNickname())){
            return ResponseEntity.badRequest().build();
        }

        String email = authentication.getName();

        Optional<User> optionalUser = userRepo.findByEmail(email);

        SellerRegister sellerRegister = SellerRegister.builder()
                .userId(optionalUser.get().getId())
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .displayName(sellerRegisterDto.getDisplayName())
                .profileDescription(sellerRegisterDto.getDescription())
                .build();

        try {
            sellerRegisterRepo.save(sellerRegister);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
