package org.hotiver.service.user;

import org.hotiver.common.Utils.HashUtils;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.user.PasswordChangeDto;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.email.EmailService;
import org.hotiver.service.redis.RedisService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UserPasswordService {

    private final CurrentUserService currentUserService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo  userRepo;

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

}
