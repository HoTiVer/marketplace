package org.hotiver.service.user;

import lombok.RequiredArgsConstructor;
import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.seller.SellerProfileDto;
import org.hotiver.repo.core.SellerRepo;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepo sellerRepo;

    public SellerProfileDto getSellerByUsername(String username) {
        Seller seller = sellerRepo.findByNickname(username)
                .orElseThrow(()-> new SellerNotFoundException("Seller not found"));

        return SellerProfileDto.builder()
                .id(seller.getId())
                .displayName(seller.getUser().getDisplayName())
                .nickname(seller.getNickname())
                .rating(seller.getRating())
                .profileDescription(seller.getProfileDescription())
                .build();
    }
}


