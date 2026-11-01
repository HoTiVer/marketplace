package org.hotiver.app.service.user;

import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.user.PersonalInfoDto;
import org.hotiver.dto.user.SecurityInfoDto;
import org.hotiver.dto.user.UserContactsDto;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setDisplayName("test");
        user.setRegisterDate(Date.valueOf(LocalDate.now()));
        user.setIsTwoFactorEnable(false);
    }

    @Nested
    class getPersonalInfo {

        @Test
        void shouldReturnPersonalInfoOfUser() {
            when(currentUserService.getCurrentUser()).thenReturn(user);

            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.empty());

            PersonalInfoDto result = userService.getPersonalInfo();

            assertEquals(user.getEmail(), result.getEmail());
            assertEquals(user.getDisplayName(), result.getDisplayName());
            assertEquals(user.getRegisterDate(), result.getRegisterDate());
        }

        @Test
        void shouldReturnPersonalInfoOfSeller() {
            Seller seller = new Seller();
            seller.setNickname("nickname");

            when(currentUserService.getCurrentUser()).thenReturn(user);

            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(seller));

            PersonalInfoDto result = userService.getPersonalInfo();

            assertEquals(user.getEmail(), result.getEmail());
            assertEquals(user.getDisplayName(), result.getDisplayName());
            assertEquals(user.getRegisterDate(), result.getRegisterDate());
            assertTrue(result.getIsSeller());
            assertEquals(seller.getNickname(), result.getSellerNickname());
        }

    }

    @Nested
    class GetUserContacts {

        @Test
        void shouldReturnUserContacts() {
            when(currentUserService.getCurrentUser()).thenReturn(user);

            UserContactsDto result = userService.getUserContacts();

            assertEquals(user.getEmail(), result.getEmail());
        }

    }

    @Nested
    class GetSecurityInfo {

        @Test
        void shouldReturnSecurityInfoTwoFactorIsDisabled() {
            when(currentUserService.getCurrentUser()).thenReturn(user);

            SecurityInfoDto result = userService.getSecurityInfo();

            assertFalse(result.getIsTwoFactorEnable());
        }

    }
}
