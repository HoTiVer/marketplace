package org.hotiver.service.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.hotiver.common.Utils.HashUtils;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.auth.ResetPasswordRequest;
import org.hotiver.dto.user.PasswordChangeDto;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.email.EmailService;
import org.hotiver.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Log4j2
@Service
public class UserPasswordService {

    private final CurrentUserService currentUserService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final static Integer MINUTES_TO_SAVE_TOKEN = 10;
    @Value("${frontend.url}")
    private String frontendHost;

    public UserPasswordService(CurrentUserService currentUserService, EmailService emailService,
                               RedisService redisService, PasswordEncoder passwordEncoder,
                               UserRepo  userRepo) {
        this.currentUserService = currentUserService;
        this.emailService = emailService;
        this.redisService = redisService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    public boolean changeUserPassword(PasswordChangeDto passwordChangeDto) {
        User user = currentUserService.getCurrentUser();

        if (user.getIsTwoFactorEnable()) {
            String key = "passwordVerify:" + HashUtils.hashKeySha256(user.getId().toString());
            String code = String.format("%06d", new Random().nextInt(999999));

            emailService.sendAsync(user.getEmail(), "Password verify", code);
            redisService.saveValue(key, code, 10);
        }

        if (passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
            userRepo.save(user);
            return true;
        }
        return false;
    }

    //TODO UPDATE LATER
//    public void verifyChangeUserPassword(PasswordChangeDto passwordChangeDto) {
//        User user = getCurrentUser();
//
//        String code = passwordChangeDto.getCode();
//        String keyToFind = "passwordVerify:" + HashUtils.hashKeySha256(user.getId().toString());
//
//        if (redisService.hasKey(keyToFind)) {
//            if (redisService.getValue(keyToFind).equals(code)) {
//
//                redisService.deleteValue(keyToFind);
//
//                user.setPassword(passwordEncoder.encode(passwordChangeDto.getPassword()));
//                userRepo.save(user);
//            }
//        }
//    }

    public void changeTwoFactorStatus() {
        User user = currentUserService.getCurrentUser();

        Boolean twoFactorStatus = user.getIsTwoFactorEnable();
        user.setIsTwoFactorEnable(!twoFactorStatus);

        userRepo.save(user);
    }

    //TODO add rate limit
    public void forgotPassword(String email) {
        log.info("Password reset requested for {}", email);
        User user = userRepo.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return;
        }

        String userTokenKey = "reset-token:" + HashUtils.hashKeySha256(user.getId().toString());
        String token = redisService.getValue(userTokenKey);
        if (token != null) {
            sendResetToken(user.getEmail(), token);
            return;
        }
        String resetToken = UUID.randomUUID().toString();

        String tokenUserKey = "reset-token:" + resetToken;
        redisService.saveValue(tokenUserKey, user.getId().toString(), MINUTES_TO_SAVE_TOKEN);

        redisService.saveValue(userTokenKey, resetToken, MINUTES_TO_SAVE_TOKEN);

        sendResetToken(user.getEmail(), resetToken);
    }

    private void sendResetToken(String email, String resetToken) {
        String resetUrl = frontendHost + "/auth/reset-password/?token=" + resetToken;
        String text = "For password changing follow this link: " + resetUrl;

        emailService.sendAsync(email, "Reset Password", text);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String tokenKey = "reset-token:" + resetPasswordRequest.getResetToken();

        String userId = redisService.getValue(tokenKey);
        if (userId == null) {
            return;
        }

        User user = userRepo.findById(Long.parseLong(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));

        userRepo.save(user);

        redisService.deleteValue(tokenKey);
        redisService.deleteValue("reset-token:"
                + HashUtils.hashKeySha256(user.getId().toString()));

        String refreshTokenKey = "refresh:" + HashUtils.hashKeySha256(userId.toString());
        redisService.deleteValue(refreshTokenKey);
    }
}
