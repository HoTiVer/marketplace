package org.hotiver.service.validation;

import org.hotiver.dto.validation.RegisterRequest.EmailUniqueChecker;
import org.hotiver.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEmailUniqueChecker implements EmailUniqueChecker {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isUnique(String email) {
        return userRepo.findByEmail(email).isEmpty();
    }
}
