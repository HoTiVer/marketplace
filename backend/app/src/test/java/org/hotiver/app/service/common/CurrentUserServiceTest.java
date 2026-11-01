package org.hotiver.app.service.common;

import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.auth.NoAuthorizationException;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.service.common.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrentUserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    public class GetCurrentUser {
        @Test
        void shouldReturnCurrentUser() {
            String userEmail = "test@test.com";
            User user = new User();
            user.setEmail(userEmail);

            Authentication auth = mock(Authentication.class);

            when(auth.getName()).thenReturn(userEmail);

            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);

            when(auth.isAuthenticated()).thenReturn(true);

            SecurityContextHolder.setContext(context);

            when(userRepo.findByEmail(userEmail))
                    .thenReturn(Optional.of(user));

            User result = currentUserService.getCurrentUser();

            assertEquals(userEmail, result.getEmail());
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            String userEmail = "test@test.com";

            Authentication auth = mock(Authentication.class);

            when(auth.getName()).thenReturn(userEmail);

            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);

            when(auth.isAuthenticated()).thenReturn(true);

            SecurityContextHolder.setContext(context);

            when(userRepo.findByEmail(userEmail))
                    .thenReturn(Optional.empty());

            assertThrows(NoAuthorizationException.class,
                    () -> currentUserService.getCurrentUser()
            );
        }

        @Test
        void shouldThrowException_whenUserUnauthorized() {
            SecurityContext context = mock(SecurityContext.class);

            SecurityContextHolder.setContext(context);

            assertThrows(NoAuthorizationException.class,
                    () -> currentUserService.getCurrentUser()
            );
        }
    }

    @Nested
    public class GetUserPrincipal {
        @Test
        void shouldReturnUserPrincipal() {
            String userEmail = "test@test.com";
            SecurityUser securityUser = createSecurityUser(userEmail);

            Authentication auth = mock(Authentication.class);

            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);

            when(auth.isAuthenticated()).thenReturn(true);

            SecurityContextHolder.setContext(context);

            when(auth.getPrincipal()).thenReturn(securityUser);

            SecurityUser result = currentUserService.getUserPrincipal();

            assertEquals(userEmail, result.getUsername());
        }

        private SecurityUser createSecurityUser(String userEmail) {
            User user = new User();
            user.setEmail(userEmail);
            user.setId(1L);
            user.setRoles(List.of(new Role(1L, RoleType.USER)));

            return new SecurityUser(user);
        }

        @Test
        void shouldThrowException_whenUserUnauthorized() {
            SecurityContext context = mock(SecurityContext.class);

            SecurityContextHolder.setContext(context);

            assertThrows(NoAuthorizationException.class,
                    () -> currentUserService.getCurrentUser()
            );
        }
    }

}
