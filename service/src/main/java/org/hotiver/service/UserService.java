package org.hotiver.service;

import org.hotiver.common.Exception.EntityAlreadyExistsException;
import org.hotiver.common.Utils.HashUtils;
import jakarta.transaction.Transactional;
import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.*;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void sendSellerRegisterRequest(SellerRegisterDto sellerRegisterDto) {
        if (!sellerRegisterDto.getRequestedNickname().matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException(
                    "Nickname may contain only English letters and numbers"
            );
        }

        if (sellerRepo.existsByNickname(sellerRegisterDto.getRequestedNickname())){
            throw new EntityAlreadyExistsException("Seller with nickname "
                    + sellerRegisterDto.getRequestedNickname()
                    + " already exists");
        }

        User user = getCurrentUser();
        Long userId = user.getId();

        if (sellerRepo.existsById(userId)){
            throw new EntityAlreadyExistsException("You are already a seller");
        }

        Date today = Date.from(Instant.now());
        if (sellerRegisterRepo.existsByUserIdAndRequestDate(userId, today)) {
            throw new EntityAlreadyExistsException("You already send a request");
        }

        if (sellerRegisterRepo.existsByUserIdAndStatus(userId, SellerRegisterRequestStatus.ACTIVE)) {
            throw new EntityAlreadyExistsException("you already send a request, wait for answer.");
        }

        SellerRegister sellerRegister = SellerRegister.builder()
                .userId(userId)
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .displayName(sellerRegisterDto.getDisplayName())
                .profileDescription(sellerRegisterDto.getDescription())
                .requestDate(Date.from(Instant.now()))
                .status(SellerRegisterRequestStatus.ACTIVE)
                .build();

        sellerRegisterRepo.save(sellerRegister);
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
            var seller = sellerRepo.findByEmail(user.getEmail()).get();
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

    public void updateUserContacts(UserContactsDto userContactsDto) {
        User user = getCurrentUser();
        String newEmail = userContactsDto.getEmail();

        if (userRepo.existsUserByEmail(newEmail))
            throw new EntityAlreadyExistsException("You cannot change your email to this");


        String code = String.format("%06d", new Random().nextInt(999999));
        String verificationCodeKey = "verification:" + HashUtils.hashKeySha256(newEmail);
        redisService.saveValue(verificationCodeKey, code, 10);
        //emailService.send(newEmail, "Validation code", code);

        String newEmailKey = "newEmail:" + HashUtils.hashKeySha256(user.getId().toString());
        redisService.saveValue(newEmailKey, newEmail, 10);
    }

    @Transactional
    public AuthResponse verifyChangingUserContacts(CodeVerifyDto codeVerifyDto) {
        User user = getCurrentUser();

        String newEmailKey = "newEmail:" + HashUtils.hashKeySha256(user.getId().toString());
        String newEmail = redisService.getValue(newEmailKey);

        String code = codeVerifyDto.getCode();
        String verificationCodeKey = "verification:" + HashUtils.hashKeySha256(newEmail);

        if (redisService.getValue(verificationCodeKey).equals(code)){
            redisService.deleteValue(verificationCodeKey);
            redisService.deleteValue(newEmailKey);

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

            return new AuthResponse(accessToken, refreshToken);
        }
        else {
            return null;
        }
    }

    public SecurityInfoDto getSecurityInfo() {
        User user = getCurrentUser();

        return SecurityInfoDto.builder()
                .isTwoFactorEnable(user.getIsTwoFactorEnable())
                .build();
    }

    public void changeTwoFactorStatus() {
        User user = getCurrentUser();

        Boolean twoFactorStatus = user.getIsTwoFactorEnable();
        user.setIsTwoFactorEnable(!twoFactorStatus);

        userRepo.save(user);
    }

    public void changeUserPassword(PasswordChangeDto passwordChangeDto) {
        User user = getCurrentUser();

        if (user.getIsTwoFactorEnable()){
            String key = "passwordVerify:" + HashUtils.hashKeySha256(user.getId().toString());
            String code = String.format("%06d", new Random().nextInt(999999));

            emailService.send(user.getEmail(), "Password verify", code);
            redisService.saveValue(key, code, 10);
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();

        user.setPassword(encoder.encode(passwordChangeDto.getPassword()));
        userRepo.save(user);
    }

    public void verifyChangeUserPassword(PasswordChangeDto passwordChangeDto) {
        User user = getCurrentUser();

        String code = passwordChangeDto.getCode();
        String keyToFind = "passwordVerify:" + HashUtils.hashKeySha256(user.getId().toString());

        if (redisService.hasKey(keyToFind)) {
            if (redisService.getValue(keyToFind).equals(code)) {

                redisService.deleteValue(keyToFind);

                PasswordEncoder encoder = new BCryptPasswordEncoder();

                user.setPassword(encoder.encode(passwordChangeDto.getPassword()));
                userRepo.save(user);
            }
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepo.findByEmail(email).orElseThrow();
    }

}