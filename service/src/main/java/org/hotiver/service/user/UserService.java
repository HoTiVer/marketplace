package org.hotiver.service.user;


import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.user.*;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.service.common.CurrentUserService;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class UserService {

    private final SellerRepo sellerRepo;
    private final CurrentUserService currentUserService;

    public UserService(SellerRepo sellerRepo, CurrentUserService currentUserService) {
        this.sellerRepo = sellerRepo;
        this.currentUserService = currentUserService;
    }

    public PersonalInfoDto getPersonalInfo() {
        User user = currentUserService.getCurrentUser();

        PersonalInfoDto personalInfoDto = PersonalInfoDto.builder()
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .registerDate(user.getRegisterDate())
                .isSeller(false)
                .build();

        Optional<Seller> seller = sellerRepo.findByEmail(user.getEmail());
        if (seller.isPresent()) {
            personalInfoDto.setIsSeller(true);
            personalInfoDto.setSellerNickname(seller.get().getNickname());
        }
        return personalInfoDto;
    }

    public UserContactsDto getUserContacts() {
        User user = currentUserService.getCurrentUser();

        UserContactsDto contactsDto = new UserContactsDto();
        contactsDto.setEmail(user.getEmail());

        return contactsDto;
    }

    public SecurityInfoDto getSecurityInfo() {
        User user = currentUserService.getCurrentUser();

        return SecurityInfoDto.builder()
                .isTwoFactorEnable(user.getIsTwoFactorEnable())
                .build();
    }
}