package org.hotiver.service.common;

import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.repo.core.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepo userRepo;

    public CurrentUserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User getCurrentUser() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NoAuthorizationException("User not found"));
    }

    public SecurityUser getUserPrincipal() {
        Authentication auth = getAuthentication();

        return (SecurityUser) auth.getPrincipal();
    }

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new NoAuthorizationException("Unauthorized");
        }
        return auth;
    }

}
