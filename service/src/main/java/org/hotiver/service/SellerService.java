package org.hotiver.service;

import org.hotiver.domain.Entity.Seller;
import org.hotiver.dto.SellerProfileDto;
import org.hotiver.repo.SellerRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepo sellerRepo;

    public SellerService(SellerRepo sellerRepo) {
        this.sellerRepo = sellerRepo;
    }

    public ResponseEntity<SellerProfileDto> getSellerById(String username) {
        Optional<Seller> opSeller = sellerRepo.findByUsername(username);

        if (opSeller.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        var seller = opSeller.get();

        SellerProfileDto sellerProfileDto = SellerProfileDto.builder()
                .id(seller.getId())
                .displayName(seller.getUser().getDisplayName())
                .nickname(seller.getNickname())
                .rating(seller.getRating())
                .profileDescription(seller.getProfileDescription())
                .build();

        return ResponseEntity.ok().body(sellerProfileDto);
    }
}
