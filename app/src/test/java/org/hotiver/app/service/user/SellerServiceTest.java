package org.hotiver.app.service.user;

import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.service.user.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SellerServiceTest {

    @Mock
    private SellerRepo sellerRepo;

    @InjectMocks
    private SellerService service;

    private Seller seller;

    @BeforeEach
    public void init() {
        User user = new User();
        user.setId(1L);
        user.setDisplayName("user");

        seller = new Seller();
        seller.setId(user.getId());
        seller.setUser(user);
        seller.setNickname("nickname");
        seller.setRating(BigDecimal.valueOf(4));
        seller.setProfileDescription("profile description");
    }

    @Nested
    class GetSellerByUsername {

        @Test
        void shouldReturnSellerProfileDto() {
            when(sellerRepo.findByNickname(seller.getNickname()))
                    .thenReturn(Optional.of(seller));

            SellerProfileDto result = service.getSellerByUsername(seller.getNickname());

            assertEquals(seller.getNickname(), result.getNickname());
            assertEquals(seller.getRating(), result.getRating());
        }

        @Test
        void shouldThrowExceptionWhenSellerIsNotExists() {
            when(sellerRepo.findByNickname(seller.getNickname()))
                    .thenReturn(Optional.empty());

            assertThrows(SellerNotFoundException.class,
                    () -> service.getSellerByUsername(seller.getNickname())
            );
        }

    }

}
