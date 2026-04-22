package org.hotiver.service.user;

import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.dto.user.*;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.email.EmailService;
import org.hotiver.service.redis.RedisService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final SellerRegisterRepo sellerRegisterRepo;
    private final SellerRepo sellerRepo;
    private final RedisService redisService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public UserService(UserRepo userRepo, SellerRegisterRepo sellerRegisterRepo,
                       SellerRepo sellerRepo, RedisService redisService,
                       EmailService emailService, JwtService jwtService,
                       CurrentUserService currentUserService) {
        this.userRepo = userRepo;
        this.sellerRegisterRepo = sellerRegisterRepo;
        this.sellerRepo = sellerRepo;
        this.redisService = redisService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
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

        SecurityUser user = currentUserService.getUserPrincipal();
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
        User user = currentUserService.getCurrentUser();

        PersonalInfoDto personalInfoDto = PersonalInfoDto.builder()
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .registerDate(user.getRegisterDate())
                .isSeller(false)
                .build();

        if (sellerRepo.existsById(user.getId())) {
            Seller seller = sellerRepo.findByEmail(user.getEmail()).orElse(null);
            personalInfoDto.setIsSeller(true);
            personalInfoDto.setSellerNickname(seller.getNickname());
        }
        return personalInfoDto;
    }

    public UserContactsDto getUserContacts() {
        User user = currentUserService.getCurrentUser();

        UserContactsDto contactsDto = new UserContactsDto();
        contactsDto.setEmail(user.getEmail());

        return contactsDto;
    }

    public SecurityInfoDto getSecurityInfo() {
        User user = currentUserService.getCurrentUser();

        return SecurityInfoDto.builder()
                .isTwoFactorEnable(user.getIsTwoFactorEnable())
                .build();
    }
}