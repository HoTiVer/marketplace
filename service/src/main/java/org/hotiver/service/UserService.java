package org.hotiver.service;

import Utils.EmailUtils;
import Utils.HashUtils;
import jakarta.transaction.Transactional;
import org.hotiver.common.SellerRegisterRequestStatus;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.*;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        if (sellerRegisterDto.getRequestedNickname() == null ||
                sellerRegisterDto.getDisplayName() == null){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "please enter requestedNickname " +
                            "and displayName"));
        }

        if (sellerRepo.existsByNickname(sellerRegisterDto.getRequestedNickname())){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "seller with this nickname exists"));
        }

        User user = getCurrentUser();
        Long userId = user.getId();

        if (sellerRepo.existsById(userId)){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "you already a seller"));
        }

        Date today = Date.from(Instant.now());
        if (sellerRegisterRepo.existsByUserIdAndRequestDate(userId, today)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "you already send a request, wait for answer."));
        }

        if (sellerRegisterRepo.existsByUserIdAndStatus(userId, SellerRegisterRequestStatus.ACTIVE)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "you already send a request, wait for answer."));
        }

        SellerRegister sellerRegister = SellerRegister.builder()
                .userId(userId)
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .displayName(sellerRegisterDto.getDisplayName())
                .profileDescription(sellerRegisterDto.getDescription())
                .requestDate(Date.from(Instant.now()))
                .status(SellerRegisterRequestStatus.ACTIVE)
                .build();

        try {
            sellerRegisterRepo.save(sellerRegister);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(Map.of("message", "successfully send request"));
    }

    public ResponseEntity<?> getNewSellerInfo() {
        User user = getCurrentUser();
        Long userId = user.getId();

        if (sellerRepo.existsById(userId)){
            return ResponseEntity.ok(Map.of("message", "you are already seller"));
        }

        return ResponseEntity.ok(Map.of("message", "you are not seller yet"));
    }

    public PersonalInfoDto getPersonalInfo() {
        User user = getCurrentUser();

        PersonalInfoDto personalInfoDto = PersonalInfoDto.builder()
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .registerDate(user.getRegisterDate())
                .isSeller(false)
                .build();

        if (sellerRepo.existsById(user.getId())) {
            var seller = sellerRepo.findByEmail(user.getEmail());
            personalInfoDto.setIsSeller(true);
            personalInfoDto.setSellerNickname(seller.getNickname());
        }
        return personalInfoDto;
    }

    public UserContactsDto getUserContacts() {
        User user = getCurrentUser();

        UserContactsDto contactsDto = new UserContactsDto();
        contactsDto.setEmail(user.getEmail());

        return contactsDto;
    }

    public ResponseEntity<?> updateUserContacts(UserContactsDto userContactsDto) {
        User user = getCurrentUser();

        String newEmail = userContactsDto.getEmail();

        if (userRepo.existsUserByEmail(newEmail))
            return ResponseEntity.badRequest().build();

        if (newEmail != null && !user.getEmail().equals(newEmail)){

            if (EmailUtils.isValidEmail(newEmail)) {
                String code = String.format("%06d", new Random().nextInt(999999));
                String key = "verification:" + HashUtils.hashKeySha256(newEmail);
                redisService.saveValue(key, code, 10);
                emailService.send(newEmail, "Validation code", code);
            }
            else {
                return ResponseEntity.badRequest().build();
            }
        }
        else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(Map.of(
                "redirect", "/cabinet/personal-info/contacts/verify",
                "method", "POST"
        ));
    }

    @Transactional
    public ResponseEntity<?> verifyChangingUserContacts(CodeVerifyDto codeVerifyDto) {
        User user = getCurrentUser();

        String newEmail = codeVerifyDto.getEmail();
        if (user.getEmail().equals(newEmail)){
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "enter new email in request"));
        }

        String code = codeVerifyDto.getCode();
        String key = "verification:" + HashUtils.hashKeySha256(codeVerifyDto.getEmail());

        if (!redisService.hasKey(key)){
            return ResponseEntity.badRequest().build();
        }

        if (redisService.getValue(key).equals(code)){
            redisService.deleteValue(key);

            user.setEmail(newEmail);
            userRepo.save(user);

            SecurityUser securityUser = new SecurityUser(user);

            String refreshToken = jwtService.generateRefreshToken(securityUser);
            Long timeToSave = jwtService.getJwtRefreshExpiration();
            String accessToken = jwtService.generateAccessToken(securityUser);

            String oldEmailKey = "refresh:" + HashUtils.hashKeySha256(user.getId().toString());
            redisService.deleteValue(oldEmailKey);

            redisService.saveValue("refresh:" + HashUtils.hashKeySha256(user.getId().toString()),
                    refreshToken, TimeUnit.MILLISECONDS.toMinutes(timeToSave));

            return ResponseEntity.ok().body(Map.of("refreshToken", refreshToken,
                    "accessToken", accessToken));
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<SecurityInfoDto> getSecurityInfo() {
        User user = getCurrentUser();

        SecurityInfoDto securityInfoDto = SecurityInfoDto.builder()
                .isTwoFactorEnable(user.getIsTwoFactorEnable())
                .build();

        return ResponseEntity.ok().body(securityInfoDto);
    }

    public ResponseEntity<?> changeTwoFactorStatus() {
        User user = getCurrentUser();

        Boolean twoFactorStatus = user.getIsTwoFactorEnable();
        user.setIsTwoFactorEnable(!twoFactorStatus);

        userRepo.save(user);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> changeUserPassword(PasswordChangeDto passwordChangeDto) {
        User user = getCurrentUser();

        if (user.getIsTwoFactorEnable()){
            String key = "passwordVerify:" + HashUtils.hashKeySha256(user.getId().toString());
            String code = String.format("%06d", new Random().nextInt(999999));

            emailService.send(user.getEmail(), "Password verify", code);
            redisService.saveValue(key, code, 10);

            return ResponseEntity.ok(Map.of(
                    "redirect", "/personal-info/security/password/verify",
                    "method", "POST"));
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();

        user.setPassword(encoder.encode(passwordChangeDto.getPassword()));
        userRepo.save(user);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> verifyChangeUserPassword(PasswordChangeDto passwordChangeDto) {
        User user = getCurrentUser();

        String code = passwordChangeDto.getCode();
        String keyToFind = "passwordVerify:" + HashUtils.hashKeySha256(user.getId().toString());

        if (redisService.hasKey(keyToFind)) {
            if (redisService.getValue(keyToFind).equals(code)) {

                redisService.deleteValue(keyToFind);

                PasswordEncoder encoder = new BCryptPasswordEncoder();

                user.setPassword(encoder.encode(passwordChangeDto.getPassword()));
                userRepo.save(user);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepo.findByEmail(email).orElseThrow();
    }

}
