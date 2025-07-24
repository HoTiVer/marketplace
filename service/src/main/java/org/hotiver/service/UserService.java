package org.hotiver.service;

import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.PersonalInfoDto;
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
        if (sellerRegisterRepo.existsByUserId(userId)){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "you already send a request"));
        }

        SellerRegister sellerRegister = SellerRegister.builder()
                .userId(userId)
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .displayName(sellerRegisterDto.getDisplayName())
                .profileDescription(sellerRegisterDto.getDescription())
                .build();

        try {
            sellerRegisterRepo.save(sellerRegister);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(Map.of("message", "successfully send request"));
    }

    public ResponseEntity<?> getNewSellerInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();
        Long userId = user.getId();

        if (sellerRepo.existsById(userId)){
            return ResponseEntity.ok(Map.of("message", "you are already seller"));
        }

        return ResponseEntity.ok(Map.of("message", "you are not seller yet"));
    }

    public PersonalInfoDto getPersonalInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();

        PersonalInfoDto personalInfoDto = PersonalInfoDto.builder()
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .registerDate(user.getRegisterDate())
                .isSeller(false)
                .build();

        if (sellerRepo.existsById(user.getId())) {
            var seller = sellerRepo.findByEmail(email);
            personalInfoDto.setIsSeller(true);
            personalInfoDto.setSellerNickname(seller.getNickname());
        }
        return personalInfoDto;
    }
}
