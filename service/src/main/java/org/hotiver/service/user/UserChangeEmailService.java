package org.hotiver.service.user;

import jakarta.transaction.Transactional;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.common.Utils.HashUtils;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.user.CodeVerifyDto;
import org.hotiver.dto.user.UserContactsDto;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.redis.RedisService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserChangeEmailService {

    private final CurrentUserService currentUserService;
    private final UserRepo userRepo;
    private final RedisService redisService;
    private final JwtService jwtService;

    public UserChangeEmailService(CurrentUserService currentUserService, UserRepo userRepo,
                                  RedisService redisService,  JwtService jwtService) {
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.redisService = redisService;
        this.jwtService = jwtService;
    }


    public void updateUserContacts(UserContactsDto userContactsDto) {
        User user = currentUserService.getCurrentUser();
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
        User user = currentUserService.getCurrentUser();

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
            Long timeToSave = jwtService.getJwtRefreshExpirationMilliseconds();
            String accessToken = jwtService.generateAccessToken(securityUser);

            String oldEmailKey = "refresh:" + HashUtils.hashKeySha256(user.getId().toString());
            redisService.deleteValue(oldEmailKey);

            redisService.saveValue("refresh:" + HashUtils.hashKeySha256(user.getId().toString()),
                    refreshToken, TimeUnit.MILLISECONDS.toMinutes(timeToSave));

            return new AuthResponse(accessToken, refreshToken,
                    jwtService.getJwtAccessExpirationMilliseconds(), jwtService.getJwtRefreshExpirationMilliseconds());
        }
        else {
            return null;
        }
    }
}
