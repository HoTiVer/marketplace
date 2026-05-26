package org.hotiver.app.factory;

import lombok.RequiredArgsConstructor;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestEntityFactory {

    private final RoleRepo roleRepo;
    private final SellerRepo sellerRepo;
    private final UserRepo userRepo;

    public Seller createSeller() {
        return sellerRepo.save(
                Seller.builder()
                    .user(createUserForSeller())
                    .nickname("seller")
                    .rating(BigDecimal.valueOf(4))
                    .profileDescription("test")
                    .products(null)
                    .build()
        );
    }

    private User createUserForSeller() {
        List<Role> roles = new ArrayList<>(
                List.of(
                        roleRepo.findByName(RoleType.USER).get(),
                        roleRepo.findByName(RoleType.SELLER).get()
                )
        );

        return userRepo.save(
                User.builder()
                    .email("seller@test.com")
                    .displayName("test")
                    .cart(null)
                    .roles(roles)
                    .balance(0.0)
                    .isTwoFactorEnable(false)
                    .registerDate(Date.valueOf(LocalDate.now()))
                    .wishlist(null)
                    .build()
        );
    }
}
