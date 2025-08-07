package org.hotiver.service;

import jakarta.transaction.Transactional;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.PersonalInfoDto;
import org.hotiver.dto.user.UserContactsDto;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final SellerRegisterRepo sellerRegisterRepo;
    private final SellerRepo sellerRepo;
    private final RedisService redisService;
    private final EmailService emailService;
    private final JwtService jwtService;

    public UserService(UserRepo userRepo, SellerRegisterRepo sellerRegisterRepo,
                       SellerRepo sellerRepo, RedisService redisService,
                       EmailService emailService, JwtService jwtService) {
        this.userRepo = userRepo;
        this.sellerRegisterRepo = sellerRegisterRepo;
        this.sellerRepo = sellerRepo;
        this.redisService = redisService;
        this.emailService = emailService;
        this.jwtService = jwtService;
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

    public UserContactsDto getUserContacts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();

        UserContactsDto contactsDto = new UserContactsDto();
        contactsDto.setEmail(user.getEmail());

        return contactsDto;
    }

    public ResponseEntity<UserContactsDto> updateUserContacts(UserContactsDto userContactsDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();

        String newEmail = userContactsDto.getEmail();

        if (userRepo.existsUserByEmail(newEmail))
            return ResponseEntity.badRequest().build();

        if (newEmail != null && !user.getEmail().equals(newEmail)){

            if (isValidEmail(newEmail)) {
                String code = String.format("%06d", new Random().nextInt(999999));
                redisService.saveValue("vce:" + email, code, 10);
                emailService.send(newEmail, "Validation code", code);
            }
            else {
                return ResponseEntity.badRequest().build();
            }
        }
        else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> verifyChangingUserContacts(CodeVerifyDto codeVerifyDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();

        String newEmail = codeVerifyDto.getEmail();
        String code = codeVerifyDto.getCode();
        String key = "vce:" + user.getEmail();

        if (redisService.getValue(key).equals(code)){
            redisService.deleteValue(key);

            user.setEmail(newEmail);
            userRepo.save(user);

            SecurityUser securityUser = new SecurityUser(user);
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", securityUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            String token = jwtService.generateToken(claims, securityUser);

            return ResponseEntity.ok().body(Map.of("token", token));
        }
        return ResponseEntity.badRequest().build();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
    }
}
