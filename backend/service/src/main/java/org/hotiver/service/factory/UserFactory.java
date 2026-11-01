package org.hotiver.service.factory;

import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class UserFactory {

    public User createNewDefaultUser(String email, String encodedPassword,
                                     String displayName, Role role) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .balance(0.0)
                .roles(List.of(role))
                .registerDate(Date.valueOf(LocalDate.now()))
                .displayName(displayName)
                .isTwoFactorEnable(false)
                .build();
    }

}
